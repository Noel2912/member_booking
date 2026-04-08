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

                // initial data load for home
                loadMembers();
                loadLessons();
            }

            if(id === 'book'){
                // ensure members select is populated
                loadMembers();
                // pre-fill lesson id if selected
                if(selectedLessonForBooking){
                    $('#book-lesson-id').val(selectedLessonForBooking);
                    selectedLessonForBooking = null;
                }
                $('#book-form').off('submit').on('submit', function(e){
                    e.preventDefault();
                    const body = { memberId: Number($('#book-member').val()), lessonId: $('#book-lesson-id').val().trim() };
                    $('#book-result').text('Booking...');
                    $.ajax({url:'/api/bookings', method:'POST', contentType:'application/json', data: JSON.stringify(body)})
                        .done(function(resp){ $('#book-result').text('Booked: '+resp.id); loadLessons(); })
                        .fail(function(xhr){ $('#book-result').text('Failed: '+xhr.responseText); });
                });
            }

            if(id === 'manage'){
                $('#change-booking').off('click').on('click', function(){
                    const id = Number($('#change-booking-id').val());
                    const newId = $('#change-new-lesson').val().trim();
                    $('#change-result').text('Changing...');
                    $.ajax({url:'/api/bookings/'+id+'/change', method:'PUT', contentType:'application/json', data: JSON.stringify({newLessonId:newId})})
                        .done(d=>$('#change-result').text('Changed'))
                        .fail(xhr=>$('#change-result').text('Failed: '+xhr.responseText));
                });

                $('#cancel-booking').off('click').on('click', function(){
                    const id = Number($('#cancel-booking-id').val());
                    $('#cancel-result').text('Cancelling...');
                    $.ajax({url:'/api/bookings/'+id, method:'DELETE'})
                        .done(d=>$('#cancel-result').text('Cancelled'))
                        .fail(xhr=>$('#cancel-result').text('Failed: '+xhr.responseText));
                });

                $('#attend-booking').off('click').on('click', function(){
                    const id = Number($('#attend-booking-id').val());
                    const rating = Number($('#attend-rating').val());
                    const review = $('#attend-review').val();
                    $('#attend-result').text('Marking attended...');
                    $.ajax({url:'/api/bookings/'+id+'/attend', method:'POST', contentType:'application/json', data: JSON.stringify({rating:rating, review:review})})
                        .done(d=>$('#attend-result').text('Marked attended'))
                        .fail(xhr=>$('#attend-result').text('Failed: '+xhr.responseText));
                });
            }

            if(id === 'report'){
                $('#report-lessons').off('click').on('click', function(){
                    const m = Number($('#report-month').val());
                    $('#report-result').text('Loading...');
                    $.get('/api/reports/month/'+m+'/lessons').done(function(data){
                        const out = $('#report-result').empty();
                        Object.entries(data).forEach(([date, lessons]) => {
                            out.append($('<h4>').text(date));
                            lessons.forEach(l => out.append($('<div>').text(l.id+' - '+l.type+' - seats:'+l.availableSeats)));
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
