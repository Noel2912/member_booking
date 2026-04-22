/* Simple UI wiring using jQuery AJAX to call /api endpoints
   Sections are loaded dynamically from /pages/<id>.html into #content.
*/
$(function(){
    let lessonsCache = [];
    let selectedLessonForBooking = null;

    function loadMembers(){
        $.get('/api/members', function(data){
            const tbl = $('#members-table tbody').empty();
            $('#book-member').empty();
            data.forEach(m => {
                const tr = $('<tr>');
                tr.append($('<td>').text(m.id));
                tr.append($('<td>').text(m.name));
                tbl.append(tr);
                const opt = $('<option>').val(m.id).text(m.id+': '+m.name);
                $('#book-member').append(opt);
            });
        });
    }

    function renderLessonsTable(list){
        lessonsCache = list || [];
        const container = $('#lessons-list').empty();
        const table = $('<table>').addClass('compact-table');
        const thead = $('<thead>');
        thead.append($('<tr>').append('<th>ID</th><th>Date</th><th>Day</th><th>Time</th><th>Type</th><th>Seats</th><th>Price</th><th>Action</th>'));
        table.append(thead);
        const tbody = $('<tbody>');
        list.forEach(l => {
            const tr = $('<tr>');
            tr.append($('<td>').text(l.id));
            tr.append($('<td>').text(l.date));
            const weekday = l.date ? new Date(l.date).toLocaleDateString(undefined, { weekday: 'long' }) : '';
            tr.append($('<td>').text(weekday));
            tr.append($('<td>').text(l.timeSlot || ''));
            tr.append($('<td>').text(l.type));
            tr.append($('<td>').text(l.availableSeats));
            tr.append($('<td>').text(l.price));
            const btn = $('<button>').addClass('action-btn').text('Select').click(()=>{
                selectedLessonForBooking = l.id;
                loadPage('book');
            });
            tr.append($('<td>').append(btn));
            tbody.append(tr);
        });
        table.append(tbody);
        container.append(table);
        // also update the booking form lesson select so user can pick when booking
        populateBookLessonSelect(lessonsCache);
    }

    function populateBookLessonSelect(list){
        const sel = $('#book-lesson-id');
        if(sel.length===0) return; // page not loaded
        sel.empty();
        sel.append($('<option>').val('').text('Select lesson...'));
        (list || []).forEach(l => {
            const label = l.id + ' - ' + (l.type || '') + (l.date ? ' ' + l.date : '') + (l.timeSlot ? ' ' + l.timeSlot : '');
            sel.append($('<option>').val(l.id).text(label));
        });
        if(selectedLessonForBooking){
            sel.val(selectedLessonForBooking);
            selectedLessonForBooking = null;
        }
    }

    function loadLessons(){
        $.get('/api/lessons', function(data){ renderLessonsTable(data); });
    }

    function showActiveTab(id){
        $('.tab').removeClass('active');
        $('#tab-'+id).addClass('active');
    }

    // load page partial into #content and wire its internal handlers
    function loadPage(id){
        $('#content').load('/pages/'+id+'.html', function(response, status){
            if(status === 'error'){
                $('#content').html('<p class="card">Failed to load page.</p>');
                return;
            }
            showActiveTab(id);

            // wire handlers per page
            if(id === 'home'){
                $('#refresh-members').off('click').on('click', loadMembers);
                $('#refresh-lessons').off('click').on('click', loadLessons);
                $('#apply-filters').off('click').on('click', function(){
                    const day = $('#day-filter').val().toLowerCase();
                    const ex = $('#exercise-filter').val().toLowerCase();
                    const q = $('#lessons-search').val().toLowerCase();
                    const filtered = lessonsCache.filter(l => {
                        const lday = l.date ? new Date(l.date).toLocaleDateString(undefined, { weekday: 'long' }).toLowerCase() : '';
                        if(day && lday !== day) return false;
                        if(ex && l.type && l.type.toLowerCase().indexOf(ex)===-1) return false;
                        if(q){
                            const hay = [l.id,l.type,l.timeSlot,l.date,lday,(l.price||'')].join(' ').toLowerCase();
                            return hay.indexOf(q) !== -1;
                        }
                        return true;
                    });
                    renderLessonsTable(filtered);
                });
                $('#lessons-search').off('input').on('input', function(){ $('#apply-filters').click(); });

                // load exercise types into dropdown and initial data load for home
                $.get('/api/exercises', function(data){
                    const sel = $('#exercise-filter').empty();
                    sel.append($('<option>').val('').text('All'));
                    data.forEach(e => {
                        // e.displayName and e.price expected from server
                        sel.append($('<option>').val(e.displayName).text(e.displayName + ' - £' + e.price));
                    });
                }).fail(function(){
                    // if exercise list fails to load, leave default 'All'
                }).always(function(){
                    loadMembers();
                    loadLessons();
                });
            }

            if(id === 'book'){
                // ensure members select is populated
                loadMembers();
                // populate lesson select from cache if available, otherwise load lessons
                if(lessonsCache && lessonsCache.length>0){
                    populateBookLessonSelect(lessonsCache);
                } else {
                    loadLessons();
                }
                $('#book-form').off('submit').on('submit', function(e){
                    e.preventDefault();
                    const body = { memberId: Number($('#book-member').val()), lessonId: $('#book-lesson-id').val().trim() };
                    $('#book-result').css('color','').text('Booking...');
                    $.ajax({url:'/api/bookings', method:'POST', contentType:'application/json', data: JSON.stringify(body)})
                        .done(function(resp){ $('#book-result').css('color','').text('Booked: '+resp.id); loadLessons(); })
                        .fail(function(xhr){
                            const text = (xhr && xhr.responseText) ? xhr.responseText : 'Booking failed';
                            if (text.toLowerCase().indexOf('already booked') !== -1 || text.toLowerCase().indexOf('duplicate') !== -1) {
                                $('#book-result').css('color','red').text('already booked');
                            } else {
                                $('#book-result').css('color','red').text('Failed: '+text);
                            }
                        });
                });
            }

            if(id === 'manage'){
                // populate booking and lesson selects for manage actions
                function populateManageLists(){
                    // load bookings
                    $.get('/api/bookings', function(bookings){
                        // only include non-cancelled bookings for actions
                        const active = (bookings || []).filter(b => b.status !== 'CANCELLED');
                        const opts = active.map(b => ({id: b.id, label: b.id + ': ' + (b.member ? b.member.name : '') + ' - lesson ' + b.lessonId}));
                        const changeSel = $('#change-booking-id');
                        const cancelSel = $('#cancel-booking-id');
                        const attendSel = $('#attend-booking-id');
                        [changeSel, cancelSel, attendSel].forEach(s => { s.empty(); s.append($('<option>').val('').text('Select booking...')); });
                        opts.forEach(o => {
                            const option = $('<option>').val(o.id).text(o.label);
                            changeSel.append(option.clone());
                            cancelSel.append(option.clone());
                            attendSel.append(option.clone());
                        });
                    }).fail(function(){
                        // clear selects on failure
                        $('#change-booking-id').empty();
                        $('#cancel-booking-id').empty();
                        $('#attend-booking-id').empty();
                    });

                    // load lessons for change-new-lesson select
                    if(lessonsCache && lessonsCache.length>0){
                        const list = lessonsCache;
                        const sel = $('#change-new-lesson').empty();
                        sel.append($('<option>').val('').text('Select lesson...'));
                        list.forEach(l => {
                            const label = l.id + ' - ' + (l.type || '') + (l.date ? ' ' + l.date : '') + (l.timeSlot ? ' ' + l.timeSlot : '');
                            sel.append($('<option>').val(l.id).text(label));
                        });
                    } else {
                        $.get('/api/lessons', function(list){
                            const sel = $('#change-new-lesson').empty();
                            sel.append($('<option>').val('').text('Select lesson...'));
                            (list || []).forEach(l => {
                                const label = l.id + ' - ' + (l.type || '') + (l.date ? ' ' + l.date : '') + (l.timeSlot ? ' ' + l.timeSlot : '');
                                sel.append($('<option>').val(l.id).text(label));
                            });
                        });
                    }
                }

                populateManageLists();

                $('#change-booking').off('click').on('click', function(){
                    const id = Number($('#change-booking-id').val());
                    const newId = $('#change-new-lesson').val();
                    if(!id || !newId){ $('#change-result').text('Please select booking and new lesson'); return; }
                    $('#change-result').text('Changing...');
                    $.ajax({url:'/api/bookings/'+id+'/change', method:'PUT', contentType:'application/json', data: JSON.stringify({newLessonId:newId})})
                        .done(function(){ $('#change-result').text('Changed'); populateManageLists(); loadLessons(); })
                        .fail(xhr=>$('#change-result').text('Failed: '+xhr.responseText));
                });

                $('#cancel-booking').off('click').on('click', function(){
                    const id = Number($('#cancel-booking-id').val());
                    if(!id){ $('#cancel-result').text('Please select booking'); return; }
                    $('#cancel-result').text('Cancelling...');
                    $.ajax({url:'/api/bookings/'+id, method:'DELETE'})
                        .done(function(){ $('#cancel-result').text('Cancelled'); populateManageLists(); loadLessons(); })
                        .fail(xhr=>$('#cancel-result').text('Failed: '+xhr.responseText));
                });

                $('#attend-booking').off('click').on('click', function(){
                    const id = Number($('#attend-booking-id').val());
                    const rating = Number($('#attend-rating').val());
                    const review = $('#attend-review').val();
                    if(!id){ $('#attend-result').text('Please select booking'); return; }
                    $('#attend-result').text('Marking attended...');
                    $.ajax({url:'/api/bookings/'+id+'/attend', method:'POST', contentType:'application/json', data: JSON.stringify({rating:rating, review:review})})
                        .done(function(){ $('#attend-result').text('Marked attended'); populateManageLists(); loadLessons(); })
                        .fail(xhr=>$('#attend-result').text('Failed: '+xhr.responseText));
                });
            }

            if(id === 'report'){
                $('#report-lessons').off('click').on('click', function(){
                    const m = Number($('#report-month').val());
                    $('#report-result').text('Loading...');
                    $.get('/api/reports/month/'+m+'/lessons').done(function(data){
                        const out = $('#report-result').empty();
                        // flatten lessons to find the highest income session
                        let all = [];
                        Object.entries(data).forEach(([date, lessons]) => { lessons.forEach(l => { l._date = date; all.push(l); }); });
                        let maxIncome = 0;
                        all.forEach(l => { if (l.income && l.income > maxIncome) maxIncome = l.income; });
                        // render grouped by date
                        Object.entries(data).forEach(([date, lessons]) => {
                            out.append($('<h4>').text(date));
                            lessons.forEach(l => {
                                const avg = (l.averageRating===null||l.averageRating===undefined)?'N/A':Number(l.averageRating).toFixed(2);
                                const div = $('<div>').addClass('report-lesson');
                                let text = l.id + ' - ' + l.type + ' - attended:' + (l.attendedCount||0) + ' - avgRating:' + avg + ' - income: £' + (l.income||0).toFixed(2);
                                div.text(text);
                                // highlight highest income session(s)
                                if (l.income && Math.abs(l.income - maxIncome) < 0.0001 && maxIncome > 0) {
                                    // append a star icon
                                    div.append($('<span>').addClass('star').css({'color':'gold','margin-left':'8px'}).text('★'));
                                }
                                out.append(div);
                            });
                        });
                    }).fail(xhr=>$('#report-result').text('Failed: '+xhr.responseText));
                });

                $('#report-income').off('click').on('click', function(){
                    const m = Number($('#report-month').val());
                    $('#report-result').text('Loading...');
                    $.get('/api/reports/month/'+m+'/income').done(function(data){
                        const out = $('#report-result').empty();
                        Object.entries(data).forEach(([k,v]) => out.append($('<div>').text(k+' = '+v)));
                    }).fail(xhr=>$('#report-result').text('Failed: '+xhr.responseText));
                });
            }
        });
    }

    // header nav wiring
    $('#tab-home').click(()=>loadPage('home'));
    $('#tab-features').click(()=>loadPage('features'));
    $('#tab-book').click(()=>loadPage('book'));
    $('#tab-manage').click(()=>loadPage('manage'));
    $('#tab-report').click(()=>loadPage('report'));

    // initial load
    loadPage('home');
});
