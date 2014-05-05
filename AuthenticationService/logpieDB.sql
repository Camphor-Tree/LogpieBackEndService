CREATE TABLE "user_auth"
(
   uid serial NOT NULL, 
   email character varying NOT NULL, 
   password text NOT NULL, 
   access_token character varying,
   access_token_expire_time timestamp with time zone,
   refresh_token character varying,
   refresh_token_expire_time timestamp with time zone,
   CONSTRAINT "PK_user_auth" PRIMARY KEY (uid) USING INDEX TABLESPACE pg_default
) 
WITH (
  OIDS = FALSE
)

TABLESPACE pg_default;
ALTER TABLE "user_auth"
  OWNER TO postgres;
  
ALTER TABLE user_auth ADD CONSTRAINT emailunique UNIQUE (email);
