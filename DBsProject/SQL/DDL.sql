create table classroom
	(building		varchar(15),
	 room_number		varchar(7),
	 capacity		numeric(4,0),
	 primary key (building, room_number)
	);

create table department
	(dept_name		varchar(20), 
	 building		varchar(15), 
	 budget		    numeric(12,2) check (budget > 0),
	 primary key (dept_name)
	);

create table course
	(course_id		varchar(8), 
	 title			varchar(50),
	 book			varchar(50), 
	 dept_name		varchar(20),
	 credits		numeric(2,0) check (credits > 0),
	 primary key (course_id),
	 foreign key (dept_name) references department (dept_name)
		on delete set null
	);

create table instructor
	(ID				varchar(5), 
	 name			varchar(20) not null, 
	 dept_name		varchar(20), 
	 salary			numeric(8,2) check (salary > 20000),
	 password		text,
	 primary key (ID),
	 foreign key (dept_name) references department (dept_name)
		on delete set null
	);

create table section
	(course_id		varchar(8), 
     sec_id			varchar(8),
	 semester		varchar(6) check (semester in ('Fall', 'Winter', 'Spring', 'Summer')),
 	 building		varchar(15),
 	room_number		varchar(5),
	 primary key (course_id, sec_id, semester),
	 foreign key (course_id) references course (course_id)
		on delete cascade
	 foreign key (building, room_number) references classroom (building, room_number)
		on delete set null,	
	);

create table lecture_time
	(start_time		time,
	 end_time		time,
	 primary key (start_time, end_time)
	);


create table lecture 
	(course_id		varchar(8), 
     sec_id			varchar(8),
	 semester		varchar(6),
	 lec_date		date,
	 lec_title		text,
	 start_time		time,
	 end_time		time,
	 primary key(course_id, sec_id, semester, lec_date),
	 foreign key (course_id, sec_id, semester) references section (course_id, sec_id, semester)
		on delete cascade,	
	 foreign key (start_time, end_time) references lecture_time (start_time, end_time)
		on delete set null	
	);

create table teaches
	(ID				varchar(5), 
	 course_id		varchar(8),
	 sec_id			varchar(8), 
	 semester		varchar(6),
	 primary key (ID, course_id, sec_id, semester),
	 foreign key (course_id, sec_id, semester) references section (course_id, sec_id, semester)
		on delete cascade,
	 foreign key (ID) references instructor (ID)
		on delete cascade
	);

create table student
	(ID					varchar(5), 
	 first_name			varchar(20) not null,
	 second_name		varchar(20) not null,	 
	 third_name			varchar(20) not null,
	 last_name			varchar(20) not null,
	 residance			varchar(20) not null,
	 dept_name			varchar(20), 
	 tot_cred			numeric(3,0) check (tot_cred >= 0),
	 primary key (ID),
	 foreign key (dept_name) references department (dept_name)
		on delete set null
	);

create table student_phones
	(ID 		varchar(5),
	phone_no	varchar(12),
	primary key (phone_no),
	foreign key (ID) references student (ID) on delete cascade
	);


create table takes
	(ID				varchar(5), 
	 course_id		varchar(8),
	 sec_id			varchar(8), 
	 semester		varchar(6),
	 primary key (ID, course_id, sec_id, semester),
	 foreign key (course_id, sec_id, semester) references section (course_id, sec_id, semester)
		on delete cascade,
	 foreign key (ID) references student (ID)
		on delete cascade
	);

create table attends
	(ID				varchar(5), 
	 course_id		varchar(8),
	 sec_id			varchar(8), 
	 semester		varchar(6),
	 lec_date		date,
	 status		    varchar(10) check (status in('present', 'absent')),
	 primary key (ID, course_id, sec_id, semester, lec_date),
	 foreign key (course_id, sec_id, semester, lec_date) references lecture (course_id, sec_id, semester, lec_date)
		on delete cascade,
	 foreign key (ID) references student (ID)
		on delete cascade
	);

create index idx_id on student(id);
create index idx_title_date on lecture(lec_title, lec_date);

CREATE OR REPLACE FUNCTION max_student_phones() 
RETURNS TRIGGER 
AS $$
BEGIN
    IF (SELECT count(*) FROM student_phones WHERE id = NEW.id) >= 2 THEN
        RAISE EXCEPTION 'Maximum number of phone numbers for a student is 2';
    END IF;
    RETURN NEW; 
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER max_student_phones_trigger
BEFORE INSERT ON student_phones
FOR EACH ROW
EXECUTE FUNCTION max_student_phones();


-- Test if a room is available. 
CREATE OR REPLACE FUNCTION is_available(sem varchar, lecture_date date,
lecture_start_time time, lecture_end_time time, building varchar, room_number varchar)
RETURNS boolean AS $$

select NOT EXISTS (
		SELECT 1 FROM lecture l natural join "section" s  WHERE 
		EXISTS (SELECT 1 WHERE ($3, $4) OVERLAPS(l.start_time, l.end_time)) and
		l.lec_date = $2 and l.semester = $1
		and s.room_number = $6 and s.building = $5
   );
$$ LANGUAGE sql;




