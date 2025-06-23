<#macro user name, password, role>
DELETE FROM users WHERE user_name = ${name?str};
DELETE FROM user_roles WHERE user_name = ${name?str};

INSERT INTO users(user_name, user_pass ) VALUES( ${name?str}, ${password?str} );
INSERT INTO user_roles VALUES( ${name?str}, ${role?str} );
</#macro>

<#macro add_role name, role>
INSERT INTO user_roles VALUES( ${name?str}, ${role?str} );
</#macro>


<@user 'admin', '12345', 'Administrator' />
<@add_role 'admin', 'SystemDeveloper' />

<@user 'User', '12345', 'User' />
