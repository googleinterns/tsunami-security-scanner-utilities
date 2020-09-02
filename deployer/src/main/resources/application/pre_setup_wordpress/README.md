# WordPress with exposed installation page

This directory contains the deployment configs for a WordPress application where
its installation page is exposed. The service listens on port `80`.

This configs deploys the following services:

- `pre-setup-wp`: the WordPress application.
- `pre-setup-wp-mysql`: the MySql database for the WordPress application.

and the following storage:

- `mysql-pv-claim`: File system required by MySql.
- `pre-setup-wp-pv-claim`: File system required by WordPress.

## Template data

```json
{
  "db_password": "password_of_your_choice"
}
```

## Example deployment command:

```shell script
# Compile the client jar if not exist. Assumes PWD is the root directory of this
project.
$ ./gradlew :client:shadowJar

java -jar client/build/libs/client.jar \
    --tsunami_testbed=[testbed master service IP and port] \
    --api_key=[API key generated at prerequisite step] \
    --operation=createDeployment \
    --app_name=pre_setup_wordpress \
    --template_data="{\"db_password\": \"password\"}"
```
