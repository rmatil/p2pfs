# README

For documentation click [here](https://bitbucket.org/raccc/comsys_challenge_task/wiki/Home)


## Project Setup

### Build Project to `jar` file

### Download and install Maven for the Command Line

If you do not already have installed maven for the cli (`mvn` should be available as cli command):

* If you are on OS X and have `brew` already installed: Type `brew install maven`
* If not, you might find maven [here](https://maven.apache.org/download.cgi)

Then execute the following command

* To build including checking tests

    ```
    mvn assembly:assembly -DdescriptorId=jar-with-dependencies
    ```

* To build without tests

   ```
   mvn assembly:assembly -DdescriptorId=jar-with-dependencies -DskipTests
   ```