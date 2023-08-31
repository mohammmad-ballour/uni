create user ballour with password 'admin' SUPERUSER;
grant all priviliges on all tables in schema "unisample" to Ballour;

CREATE ROLE teaching_assistant;
GRANT USAGE ON schema unisample to teaching_assistant;
GRANT SELECT, INSERT, UPDATE, DELETE ON unisample.lecture TO teaching_assistant;
GRANT SELECT, INSERT, UPDATE, DELETE ON unisample.attends TO teaching_assistant;
GRANT SELECT, INSERT, UPDATE, DELETE ON unisample.student  TO teaching_assistant;
GRANT SELECT, INSERT, UPDATE, DELETE ON unisample.student_phones TO teaching_assistant;


CREATE OR REPLACE FUNCTION revoke_authorities(userid varchar)
RETURNS void AS $$
DECLARE
   	user_name text;
BEGIN
    
   	SELECT name INTO user_name from instructor WHERE id = userid;
    -- Rvoke privileges from the user
    EXECUTE format('REVOKE SELECT, INSERT, UPDATE, DELETE ON unisample.lecture FROM %I', (user_name || userid));
    EXECUTE format('REVOKE SELECT, INSERT, UPDATE, DELETE ON unisample.attends FROM %I', (user_name || userid));
   	EXECUTE format('REVOKE SELECT, INSERT, UPDATE, DELETE ON unisample.student FROM %I', (user_name || userid));
    EXECUTE format('REVOKE SELECT, INSERT, UPDATE, DELETE ON unisample.student_phones FROM %I', (user_name || userid));
    
    -- Drop the user from the role
    EXECUTE format('REVOKE teaching_assistant FROM %I', (user_name || userid));
END;
$$ LANGUAGE plpgsql;


-- updated 
CREATE OR REPLACE FUNCTION create_user_and_grant_authorities(userid varchar)
RETURNS void AS $$
DECLARE
    user_password text;
   	user_name text;
BEGIN
    SELECT password INTO user_password from instructor WHERE id = userid;
   	SELECT name INTO user_name from instructor WHERE id = userid;
	
    -- Create the new user with the retrieved password
    EXECUTE format('CREATE USER %I WITH PASSWORD %L', (user_name || userid), quote_literal(user_password));
   
    -- Grant privileges to the user
    EXECUTE format('GRANT SELECT, INSERT, UPDATE, DELETE ON unisample.lecture TO %I', (user_name || userid));
    EXECUTE format('GRANT SELECT, INSERT, UPDATE, DELETE ON unisample.attends TO %I', (user_name || userid));
   	EXECUTE format('GRANT SELECT, INSERT, UPDATE, DELETE ON unisample.student TO %I', (user_name || userid));
    EXECUTE format('GRANT SELECT, INSERT, UPDATE, DELETE ON unisample.student_phones TO %I', (user_name || userid));
    
    -- Add the user to the role
    EXECUTE format('GRANT teaching_assistant TO %I',  (user_name || userid));
END;
$$ LANGUAGE plpgsql;











