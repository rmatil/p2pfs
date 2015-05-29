# README

For documentation click [here](https://bitbucket.org/raccc/comsys_challenge_task/wiki/Home)


## Project Setup

### Build Project to `jar` file

If you do not already have installed maven for the cli (`mvn` should be available as cli command):

* If you are on OS X and have `brew` already installed: Type `brew install maven`
* If not, you might find maven [here](https://maven.apache.org/download.cgi)

Then execute the following command

* To build including checking tests

    ```
    mvn assembly:assembly -DdescriptorId=jar-with-dependencies
    ```

* To build without checking tests

    ```
    mvn assembly:assembly -DdescriptorId=jar-with-dependencies -DskipTests
    ```
    
# LICENSE

   Copyright 2015 Raphael Matile, Reto Wettstein, Christian Tresch, Samuel von Baussnern

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
    
