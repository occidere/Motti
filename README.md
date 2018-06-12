# Motti
Multi-threaded web files download library for Java

# Dependency
## Maven Repository
````
<repositories>
	<repository>
		<id>Motti-snapshot</id>
		<url>https://github.com/occidere/occidere-maven-repo/tree/master/snapshots</url>
	</repository>
</repositories>
````

## Maven Dependency
````
<dependencies>
    <dependency>
        <groupId>org.occidere</groupId>
        <artifactId>Motti</artifactId>
        <version>0.0.0.3</version>
    </dependency>
</dependencies>
````

# Usage
## Example
````
package com.example.MyTest;

import motti.Motti;
import motti.MottiImpl;

public class App {
	public static void main(String[] args) throws Exception {
		String target = "https://github.com/occidere/MMDownloader/releases/download/v0.5.1.0/MMDownloader-0.5.1.0-beta.jar";
		String savePath = "C://test//MMDownloader-0.5.1.0-beta.jar";
		
		Motti motti = new MottiImpl();
		motti.setThreadCount(8);
		
		motti.download(target, savePath);
	}
}
````
