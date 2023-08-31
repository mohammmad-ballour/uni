select t.id, t.course_id, a.lec_date, status 
from takes t join attends a using(id, course_id, sec_id, semester)
order by 1
--where course_id = 'CS305'and sec_id = '101' and semester = 'Fall'

--tests that all students are enrolled in the sections they attend (for data consistency)
select a.id, a.course_id, a.sec_id, a.semester, t.course_id, t.sec_id, t.semester from 
takes t join attends a using (course_id, sec_id, semester)
where (t.course_id, t.sec_id, t.semester) <> (a.course_id, a.sec_id, a.semester)

-- insert_lecture is dropped, Just use parameters bellow for testing
--will not insert the lecture because FIN404_102 are in Economics100 in (12:00 - 14:00)
select insert_lecture('FIN100', '102', 'Spring', '2022-03-08', 'TEST', '11:00', '14:00', 'Economics', '100');

--will not insert the lecture because CS212_102 are in IT230 in (08:00 - 10:00)
select insert_lecture('CS305', '102', 'Fall', '2022-12-02', 'New Lecture', '08:00', '11:00', 'IT', '230'); 

--will insert 
select insert_lecture('FIN100', '102', 'Spring', '2022-03-08', 'Introduction to Accounting', '08:00', '10:00', 'Economics', '100');


-- students attending < 25% is already supported in the GUI

-- top 3 students (in a given semester) && order by commitments with omitting limit clause
with cte(id, cnt) as (
select id, count(case when status = 'present' then 1 end) as cnt
from attends a 
where semester = 'Spring'
group by id
)
select * from cte 
order by cte.cnt desc limit 3;

-- for those who attended more than 70%, find the lectures they missed (in a given course)
with cte(id, cnt, ratio) as (
select id, count(case when status = 'present' then 1 end) as cnt, 
count(case when status = 'present' then 1 end) / count(*)::float as ratio from attends 
where course_id = 'CS212' and semester = 'Fall'
group by 1 order by 1
)
select cte.id, a.lec_date, l.lec_title 
from cte natural join attends a natural join lecture l
where cte.ratio > 0.70 and course_id = 'CS212' and semester = 'Fall' and a.status = 'absent'
order by 1, 2;

-- missing > attending
select l.lec_date , l.lec_title  
from lecture l natural join attends a 
where course_id = 'CS301'	-- for testing purposes
group by 1, 2
having count(case when status = 'absent' then 1 end) > count(case when status = 'present' then 1 end)
order by 1;

-- students absent in 3 consecutive lectures (in a given course)
with cte(id, lec_date, next_absence, prev_absence) as (
select id, lec_date , lead(lec_date) over(partition by id order by lec_date) as next_absence,
lag(lec_date) over(partition by id order by lec_date) as prev_absence
from attends a 
where course_id = 'FIN320' and sec_id = '102' and semester = 'Fall' and status = 'absent' 
)
select distinct id from cte
where cte.next_absence = cte.prev_absence + interval '2 weeks'; 


-- LEAVE THIS TO ME.
select id, s.first_name || ' ' || s.second_name || ' ' || s.third_name || ' ' || s.last_name as full_name, sp.phone_no  
from takes t natural join student s natural join student_phones sp
where course_id = 'CS301' and sec_id = '101' and semester = 'Spring';


select a.id, a.lec_date , a.status 
from attends a		
where a.course_id = 'CS301' and a.sec_id = '101' and a.semester = 'Spring' and a.lec_date = '2022-03-08';

select id, s.first_name || ' ' || s.second_name || '. ' || s.third_name || '. ' || s.last_name as full_name ,
count(case when status = 'present' then 1 end), count(case when status = 'present' then 1 end) / count(*)::float as ratio
from attends natural join student s 
where course_id = 'CS212' and semester = 'Fall'
group by 1,2 order by 1;

SELECT distinct lec_title FROM lecture WHERE course_id = 'CS212'


select * from pg_roles where rolcanlogin = true;
select * from pg_shadow;
SELECT conname FROM pg_constraint WHERE conrelid = 'section'::regclass AND contype = 'f';
select exists (select 1 from  pg_roles where rolname = 'Usama00101'); 
select (has_table_privilege('Usama00101', 'attends', 'DELETE') and has_table_privilege('Usama00101', 'lecture', 'INSERT'));


select revoke_authorities('00111');
drop user "Abdallah00111";