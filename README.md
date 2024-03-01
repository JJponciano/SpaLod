# SpaLod
Spatial data management with semantic web technology and Linked Open Data (LOD)
# Spalod (Spatial data management with semantic web technology and Linked Open Data): A Platform for Integrating GIS Files into Web Semantics through GraphDB

## Introduction
SpaLOD addresses the increasingly complex challenge brought about by the rapid growth of geospatial data, which expands by at least 20% every year. This has resulted in an enormous increase in data heterogeneity, creating complexities in structure and vocabulary variations. The vocabularies in use depend heavily on the application domain and the language in which the data is described, making integration and unification a daunting task.

In light of these challenges and harnessing the potential of Semantic Web technologies, numerous approaches have emerged to group these data into knowledge graphs. These knowledge graphs enable efficient data linking, ease sharing, and enhance maintenance. However, they also bring forth the daunting task of data homogenization due to the non-unified data structures and vocabulary variations.

To overcome this problem of homogenization, we present SpaLOD, a comprehensive framework designed to efficiently group heterogeneous spatial data into a single knowledge base. The knowledge base is rooted in an ontology connected to Schema.org and DCAT-AP, providing a data structure compatible with GeoSPARQL. One of the unique strengths of SpaLOD is its ability to integrate geospatial data independently of their original language. This is made possible by translating them using advanced Neural Machine Translation.

SpaLOD sets a new benchmark in the field of geospatial data, enabling a universal sharing platform and fostering collaboration between different states and organizations globally. Through SpaLOD, we envision a future where geospatial data can be universally used, integrated, and shared, regardless of their original structure and language.

In the domain of geospatial data management and analysis, the integration of Geographic Information System (GIS) files with web semantic technologies represents a notable advancement. Spalod emerges as a platform conceived with the purpose of facilitating this integration through the use of GraphDB. It aims to enhance the accessibility and utility of geospatial data within web environments, leveraging the inherent capabilities of graph databases to enrich the analysis, interaction, and visualization of spatial information. Developed upon a foundation of GraphDB, Spring, and Vue.js, Spalod aspires to serve as a bridge between the realms of geospatial data and semantic web technologies.

### Platform Objectives

Spalod is designed with several key objectives in mind:
- To simplify the process of importing GIS files into a graph database environment, thereby enabling more sophisticated queries and analyses that exploit the semantic relationships within spatial data.
- To provide a robust, scalable solution capable of managing extensive datasets while ensuring a responsive and intuitive user experience.
- To foster a community-driven development approach, encouraging open-source contributions and feedback, thereby facilitating continuous improvement and innovation.

### System Requirements

For optimal operation, Spalod necessitates:
- **GraphDB**: Utilized for the storage and querying of semantic geospatial data.
- **Spring Framework**: Employs the backend architecture, ensuring application security, robustness, and scalability.
- **Vue.js**: Enhances the frontend with a dynamic and user-friendly interface.

### Intended Audience

The platform is tailored for a diverse audience, including but not limited to developers, data scientists, GIS specialists, and enthusiasts at the intersection of geospatial data and semantic web technologies. It is particularly suited for individuals and organizations engaged in sophisticated geospatial data analysis, application development leveraging spatial data, or those intrigued by the potential of marrying GIS with semantic web technologies.

### Documentation Overview

This documentation is meticulously crafted to assist users in swiftly setting up Spalod, comprehending its architectural framework, and thoroughly understanding its functionalities. The documentation is structured as follows:

1. **Set Up and Installation**: Provides comprehensive guidance for installing and configuring Spalod on various systems.
2. **Structure Overview**: Offers insights into the architectural design of Spalod, elucidating the components and their interconnections.
3. **Code Explanation**: Delivers in-depth explanations of critical segments of Spalod's codebase, facilitating a deeper understanding of its operational mechanisms and potential for extension.

---

# Set Up and Installation

## Installing Spring for Spalod

This section provides a comprehensive guide to installing the Spring framework, which serves as the backbone for the backend development of Spalod. Spring facilitates the creation of high-performing, reusable, and easily testable code, making it an integral component of the Spalod platform. The instructions are tailored for a broad audience, assuming minimal prior experience with Spring.

### Prerequisites

Before proceeding with the Spring installation, ensure that the following prerequisites are met:

- **Java Development Kit (JDK)**: Spring requires JDK 8 or newer. Verify your Java version by running `java -version` in your command line interface (CLI).
- **Maven or Gradle**: These are the most commonly used build tools for managing dependencies and building Java projects. Maven is used in the examples below, but Gradle could also be used interchangeably.
- **Integrated Development Environment (IDE)**: While optional, using an IDE such as IntelliJ IDEA, Eclipse, or Spring Tool Suite (STS) can significantly streamline your Spring application development.

### Step 1: Installing Java Development Kit (JDK)

1. **Download JDK**: Visit the [official Oracle website](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html) or adopt an OpenJDK variant like [AdoptOpenJDK](https://adoptopenjdk.net/).
2. **Install JDK**: Run the installer and follow the setup wizard.
3. **Set JAVA_HOME**:
   - **Windows**: Set `JAVA_HOME` in your system environment variables to point to your JDK installation directory.
   - **macOS/Linux**: Add `export JAVA_HOME=/path/to/your/jdk` to your `.bashrc`, `.bash_profile`, or `.zshrc` file.

### Step 2: Installing Maven

1. **Download Maven**: Navigate to the [Apache Maven Project website](https://maven.apache.org/download.cgi) and download the latest version.
2. **Extract Files**: Extract the downloaded archive to your preferred location.
3. **Configure Environment Variables**:
   - **Windows**: Add the `bin` directory of the extracted Maven to the `Path` variable in system environment settings.
   - **macOS/Linux**: Add `export PATH=/path/to/apache-maven/bin:$PATH` to your shell configuration file.

### Step 3: Setting Up Spring

While Spring does not require an explicit installation, setting up a Spring-based project involves configuring the project structure and dependencies:

#### Using Spring Initializr

Spring Initializr provides an easy way to create a project skeleton:

1. **Navigate to [Spring Initializr](https://start.spring.io/)**.
2. **Customize Your Project**: Select your preferred project metadata (Group, Artifact, Name, Description, etc.), Spring Boot version, packaging (Jar is recommended), Java version (aligned with your JDK), and dependencies (Spring Web, Spring Data JPA, etc.).
3. **Generate Project**: Click on "Generate" to download a `.zip` file containing the project skeleton.
4. **Extract and Open Project**: Extract the project and open it in your IDE.

#### Manual Setup

For a more hands-on approach, you can manually create your project structure and `pom.xml` (for Maven) or `build.gradle` (for Gradle) files to manage dependencies.

### Step 4: Verifying Installation

To verify that your Spring setup is correct:

1. **Navigate to Your Project Directory** in your CLI.
2. **Run Your Application**: Execute `mvn spring-boot:run` for Maven projects or `gradle bootRun` for Gradle projects.
3. **Access the Running Application**: Open a web browser and visit `http://localhost:8080`. If you have a controller mapped to the root, you should see the response from your application.

### Conclusion

You have successfully set up your environment for Spring development. This foundational step is crucial for developing the backend services for Spalod, enabling you to proceed with creating a robust and scalable platform. The Spring ecosystem is vast and equipped with extensive documentation and community support to guide you through any challenges that may arise.

---

## Installing Vue.js for Spalod

This section outlines the process for setting up Vue.js, which serves as the cornerstone for the front-end development of Spalod. Vue.js is a progressive JavaScript framework used for building user interfaces and single-page applications. It is known for its ease of integration, scalability, and a vibrant supporting ecosystem.

### Prerequisites

Before installing Vue.js, ensure you have the following:

- **Node.js and npm**: Vue.js requires Node.js. npm (Node Package Manager) is included with Node.js and is used to manage JavaScript packages.
- **A text editor or IDE**: While not strictly necessary for Vue.js itself, having a good code editor (like Visual Studio Code, Atom, or Sublime Text) or an IDE will make coding more manageable.

### Step 1: Installing Node.js and npm

1. **Download Node.js**: Visit the [official Node.js website](https://nodejs.org/) and download the installer for your operating system. The npm package manager is included with Node.js.
2. **Install Node.js and npm**: Run the downloaded installer, following the prompts to complete the installation.
3. **Verify Installation**: Open your command line interface (CLI) and run the following commands to check that Node.js and npm are correctly installed:
   ```bash
   node -v
   npm -v
   ```
   These commands should display the installed versions of Node.js and npm, respectively.

### Step 2: Installing Vue CLI

The Vue Command Line Interface (Vue CLI) is a powerful tool that facilitates the development of Vue.js applications. It helps in scaffolding new projects, managing configurations, and performing common tasks related to Vue development.

1. **Install Vue CLI**: Run the following command in your CLI to install Vue CLI globally:
   ```bash
   npm install -g @vue/cli
   ```
2. **Verify Vue CLI Installation**: Check that the installation was successful by running:
   ```bash
   vue --version
   ```
   This command will display the installed version of Vue CLI.

### Step 3: Creating a New Vue.js Project

With Vue CLI installed, you can easily create a new Vue.js project:

1. **Create Project**: Navigate to the directory where you want your project to be located, then run:
   ```bash
   vue create your-project-name
   ```
   Replace `your-project-name` with the desired name for your project. Vue CLI will prompt you to pick a preset. You can choose the default preset or manually select features for your project.
   
2. **Navigate to Your Project**: Change into your project directory:
   ```bash
   cd your-project-name
   ```

3. **Serve Your Project**: Start the development server with:
   ```bash
   npm run serve
   ```
   This command compiles and hot-reloads your project for development. You can view your application by opening `http://localhost:8080/` in a web browser.

### Step 4: Exploring Vue.js

With your Vue.js environment set up and your initial project ready, you are well-positioned to start developing the front-end part of Spalod. Vue.js's documentation is an excellent resource for learning about Vue.js features, components, and best practices. Engage with the Vue.js community through forums and social media for support and to keep up with the latest in Vue.js development.

### Conclusion

You have successfully set up Vue.js for front-end development within the Spalod platform. This setup enables you to leverage Vue.js's capabilities for building dynamic, efficient, and sophisticated user interfaces.  

---

## Installing GraphDB for Spalod

This guide provides detailed instructions on setting up GraphDB, a powerful graph database that serves as the data storage and querying backbone for Spalod. GraphDB is designed to handle complex, interconnected data, making it an ideal choice for managing geospatial information and integrating it with web semantic technologies.

### Prerequisites

Before installing GraphDB, ensure you have the following prerequisites:

- **Java Runtime Environment (JRE)**: GraphDB requires a JRE to run. The specific version required can vary based on the GraphDB version you plan to install, so refer to the official documentation for the exact requirements.
- **Sufficient hardware resources**: While the exact requirements will depend on the size of your datasets and the complexity of your queries, a modern processor, 4GB of RAM (8GB or more recommended), and sufficient disk space for your data are advisable starting points.

### Step 1: Downloading GraphDB

1. **Visit the Official Website**: Go to the [GraphDB download page](http://ontotext.com/products/graphdb/) to find the latest version of GraphDB.
2. **Select the Edition**: GraphDB is available in several editions (Free, Standard, and Enterprise). The Free edition is suitable for many projects and is a good starting point.
3. **Download**: After selecting the edition, download the GraphDB distribution package suitable for your operating system (Windows, macOS, or Linux).

### Step 2: Installing GraphDB

#### Windows

1. **Extract the ZIP File**: Once downloaded, extract the ZIP file to your desired location.
2. **Run GraphDB**: Navigate to the extracted folder and run `graphdb.bat` to start the GraphDB server.

#### macOS/Linux

1. **Extract the Archive**: Use the command line or a file manager to extract the downloaded TAR.GZ file.
2. **Run GraphDB**: Open a terminal, navigate to the extracted folder, and execute `./graphdb` to start the GraphDB server.

### Step 3: Verifying the Installation

After starting GraphDB, verify that it is running correctly:

1. **Open a Web Browser**: Navigate to `http://localhost:7200`. This address brings you to the GraphDB Workbench, a web-based interface for managing your databases.
2. **Create a Repository**: Try creating a new repository to confirm that GraphDB is functioning correctly. The process involves clicking on the "Setup" menu, selecting "Repositories", and then "Create new repository".

### Step 4: Configuring GraphDB for Spalod

To optimize GraphDB for use with Spalod, consider the following configurations:

- **Repository Creation**: Create a dedicated repository for your Spalod project, choosing a repository ID and title that reflect your project's name or purpose.
- **Import Data**: Import your GIS files into GraphDB. Depending on the format of your GIS data, this may require converting the files into a format compatible with GraphDB (e.g., RDF) before import.
- **Indexing and Performance Tuning**: Explore GraphDB's indexing options and performance tuning settings to optimize query performance, especially important for large datasets.

### Conclusion

You have successfully installed GraphDB and performed basic configuration steps to get started with Spalod. This setup allows you to leverage the powerful features of GraphDB for storing, querying, and managing geospatial data within a semantic framework.

---

## Installing SPALOD

This part of the documentation covers the installation process for SPALOD. The installation involves setting up the necessary libraries, packaging the application, and finally running SPALOD along with its components. Ensure that you have Maven, GraphDB, and Node.js (with npm) installed on your system before proceeding.

## Installing Required Libraries

SPALOD requires specific libraries to be installed in your local Maven repository. These libraries include `pisemantic` and `pitools`, which are essential for the platform's operation.

### Installing pisemantic

1. Open a terminal or command prompt.
2. Navigate to the root directory of your SPALOD project.
3. Execute the following Maven command to install the `pisemantic` library:

   ```shell
   mvn install:install-file \
   -Dfile=libs/pisemantic-1.0-SNAPSHOT.jar \
   -DpomFile=pom.xml \
   -DgroupId=info.ponciano.lab \
   -DartifactId=pisemantic \
   -Dversion=1.0 \
   -Dpackaging=jar
   ```

### Installing pitools

After installing the `pisemantic` library, proceed with the `pitools` library using a similar command:

```shell
mvn install:install-file \
-Dfile=libs/pitools-1.0-SNAPSHOT.jar \
-DpomFile=pom.xml \
-DgroupId=info.ponciano.lab \
-DartifactId=pitools \
-Dversion=1.0-SNAPSHOT \
-Dpackaging=jar
```

## Packaging SPALOD

With the necessary libraries installed, the next step is to package SPALOD using Maven. This step compiles the project and prepares it for execution.

1. In the terminal, ensure you are still in the SPALOD project root directory.
2. Execute the Maven package command, skipping the tests for faster execution:

   ```shell
   mvn package -DskipTests
   ```

## Running GraphDB

Before running SPALOD, you need to start GraphDB:

1. Navigate to the GraphDB installation directory and locate the `bin` folder.
2. Start GraphDB by running the following command (adjust the version number as necessary):

   ```shell
   ./graphdb-10.2.1/bin/graphdb
   ```

Ensure GraphDB is running successfully by accessing its web interface, usually available at `http://localhost:7200`.

## Running SPALOD

With GraphDB running, you can now start SPALOD:

1. In the terminal, navigate back to the SPALOD project root directory.
2. Run the SPALOD application using the following command:

   ```shell
   java -jar target/spalod-0.0.1-SNAPSHOT.jar
   ```

## Setting up the Front-End

The final step involves setting up the Vue.js front-end for SPALOD:

1. Navigate to the Vue.js project directory within SPALOD:

   ```shell
   cd /home/spalod/src/main/vue_js/spalod
   ```

2. Install the necessary npm packages:

   ```shell
   npm install
   ```

3. Start the Vue.js development server:

   ```shell
   npm run dev -- --host
   ```

## Accessing SPALOD

Once everything is up and running, access the SPALOD platform by navigating to `https://localhost:8080` in your web browser. You should now see the SPALOD interface, ready for use.

## Conclusion

You have successfully installed and started SPALOD, along with its dependencies. This setup allows you to integrate GIS files into web semantics through GraphDB effectively. As you proceed, explore SPALOD's features and functionalities to fully leverage its capabilities.

---

# Structure Overview
The SPALOD platform is architecturally designed to separate concerns between the front-end and back-end, ensuring a modular and maintainable codebase. This separation is achieved by dividing the platform's structure into two main parts: the back-end, developed using the Spring framework and located in `src/main/java`, and the front-end, built with Vue.js and housed in `src/main/vue_js`. The back-end in Spring is responsible for handling business logic, database operations, and server-side functionalities, serving as the backbone of SPALOD's data processing and storage capabilities. Conversely, the Vue.js front-end focuses on the user interface and user experience, providing an interactive and responsive web application that communicates with the Spring back-end through API calls. This clear division allows for independent development and scaling of both sides of the application, facilitating collaboration among teams and streamlining the development process.

## SPALOD Front-End Structure Overview

The SPALOD platform features a sophisticated front-end, built with Vue.js, designed to deliver a seamless user experience for integrating GIS files into web semantics through GraphDB. This section provides an overview of the front-end structure, highlighting key directories and files that contribute to the functionality and appearance of the SPALOD interface.

### Project Structure

The front-end of SPALOD is located under `src/main/vue_js/spalod`, containing several critical files and directories essential for the development and deployment of the user interface:

- **`Dockerfile`**: Defines the Docker container that can be used to deploy the SPALOD front-end in a consistent and isolated environment.
- **`index.html`**: The entry point for the SPALOD web application. It loads the compiled Vue.js application.
- **`README.md`**: Provides documentation specific to the front-end, including setup instructions and general information about the Vue.js project structure.
- **`new-server.key`**, **`server.crt`**, **`server.key`**: These files are related to HTTPS configuration for secure communication.
- **`npm`**, **`package-lock.json`**, **`package.json`**: These files are used by npm (Node Package Manager) to manage project dependencies.
- **`public`**: Contains static assets that are served directly by the web server, such as favicon.
- **`src`**: The core directory where the Vue.js application's source code is located, including scripts, components, and assets.
- **`vite.config.js`**: Configuration file for Vite, a modern front-end build tool that significantly improves the development experience.

### Source Directory Structure

Within the `src` directory of the SPALOD front-end, you'll find the following subdirectories and files:

- **`App.vue`**: The main Vue component that serves as the entry point for the Vue.js application.
- **`assets`**: Contains static resources like CSS files and images that enhance the UI's look and feel.
- **`components`**: This directory houses the Vue components that make up the UI. Each component is designed for a specific feature or UI element, making the codebase modular and maintainable.
- **`config.js`**: Contains configuration settings for the SPALOD front-end, potentially including API endpoints, default settings, and other constants.
- **`main.js`**: The JavaScript entry point that initializes the Vue application, including setting up plugins and mounting the root component.
- **`pictures`**: This directory may contain additional images used throughout the application, separate from those in `assets` for organizational purposes.

#### Assets

The `assets` directory includes:

- **`base.css`** and **`main.css`**: CSS files that define global and main styles for the application, ensuring a consistent look and feel across the platform.
- **`bkg_i3mainz.png`**, **`figure1.jpg`**, **`system-functionality.png`**: Images used within the application, potentially for backgrounds, illustrations, and functional diagrams.

#### Components
The `components` directory under `src/main/vue_js/spalod/src` houses the Vue components that are integral to the platform's functionality. This section provides a brief overview of each component, detailing its purpose and role within SPALOD.

##### `Dataset.vue`

This component is responsible for managing and displaying information related to GIS datasets. It allows users to upload, view, and manipulate GIS data, making it central to SPALOD's core functionality of integrating GIS files into web semantics.

##### `Docs.vue`

The `Docs.vue` component serves as a documentation viewer within the platform. It provides users with guides, API documentation, and other helpful information about using SPALOD effectively.

##### `ExternalLinks.vue`

This component offers quick access to external resources relevant to SPALOD users. It could include links to GIS databases, semantic web resources, or other related tools and platforms.

##### `Login.vue`

The `Login.vue` component handles user authentication. It presents a form where users can enter their credentials to access SPALOD's features that require authentication.

##### `MapView.vue`

`MapView.vue` is integral to visualizing GIS data on a map. This component integrates map services to display geographic information, allowing users to interact with and analyze GIS data spatially.

##### `NavBar.vue`

The navigation bar, implemented by `NavBar.vue`, provides the primary means of navigating the SPALOD platform. It includes links to the various components and pages within SPALOD, ensuring users can easily find and access the platform's features.

##### `OgcApi.vue`

This component is designed to interact with Open Geospatial Consortium (OGC) APIs, facilitating the retrieval and manipulation of GIS data according to OGC standards. It plays a critical role in ensuring SPALOD's compatibility with widely used geospatial data services.

##### `PopUp.vue` and `PopUpC.vue`

These components are used to display modal pop-ups within the application. They can be utilized for various purposes, such as showing detailed information about a dataset, confirming user actions, or presenting forms for data entry.

##### `RDFData.vue`

`RDFData.vue` focuses on displaying and managing RDF (Resource Description Framework) data components. It allows users to explore and manipulate semantic data, linking GIS data with web



### Conclusion

The SPALOD front-end structure is designed to offer a comprehensive and user-friendly interface for integrating GIS files into web semantics. By understanding the layout and functionality provided by each file and directory, developers can effectively navigate, modify, and enhance the SPALOD platform to meet their needs or extend its capabilities.

---

## SPALOD Backend Structure Overview

The SPALOD platform's backend, built using Spring Boot, orchestrates the integration of GIS files into web semantics through GraphDB. The backend structure is organized into several key directories under `src/main/java/info/ponciano/lab/spalodwfs/`, each containing Java classes that serve specific roles within the application. This section outlines the purpose and functionality of these directories and their respective classes, excluding the `mvc` folder, which is not utilized in the current version of SPALOD.

### Main Components

#### `SpalodwfsApplication.java`

This is the main entry point for the Spring Boot application. It bootstraps the SPALOD application, setting up the necessary configuration for the service to run.

#### `WebConfig.java`

Configures web-related aspects of the application, such as CORS (Cross-Origin Resource Sharing) settings, to ensure the frontend can communicate with the backend without security issues.

### Directory: `controller`

The `controller` directory contains controllers that handle HTTP requests and direct them to the appropriate services.

- **`ResController.java`**: Manages general resource requests, serving as an entry point for various data operations within SPALOD.
- **`ogc_api`**: Contains controllers specific to handling requests compliant with Open Geospatial Consortium (OGC) standards.
- **`security`**: Manages authentication and authorization, ensuring secure access to SPALOD's resources.
- **`storage`**: Handles file storage and retrieval, crucial for managing GIS files and related data.

### Directory: `model`

The `model` directory defines the data models and utilities for handling GIS data, semantic web data, and other forms of data manipulation.

- **`DatasetDownlift.java`**: Converts high-level semantic data into a more accessible format for GIS applications.
- **`JsonToGeoJson.java`**: Transforms JSON data into GeoJSON, a format widely used for encoding geographic data structures.
- **`KBmanager.java` & `KBmanagerLocal.java`**: Manage interactions with the Knowledge Base (GraphDB), facilitating data storage, retrieval, and manipulation in the semantic layer.
- **`OntologyGenerator.java`**: Dynamically generates ontologies based on the data models and relationships within SPALOD.
- **`SemData.java`, `TripleData.java`, `TripleOperation.java`, `Triplestore.java`**: Handle the creation, manipulation, and storage of RDF triples, essential for linking GIS data with web semantics.
- **`Sparql.java`, `SparqlQuery.java`**: Provide utilities for constructing and executing SPARQL queries against the RDF data stored in GraphDB.
- **`QueryResult.java`**: Represents the results of SPARQL queries in a structured format.

### Directory: `services`

The `services` directory encapsulates the business logic of the application, interacting with models to process data and serve the controllers.

- **`FormDataService.java`**: Handles the processing and management of form data submissions, likely related to user input or data import forms within SPALOD.

### Conclusion

The backend of the SPALOD platform is designed with a clear separation of concerns, dividing the application into controllers for handling requests, models for defining data structures and operations, and services for implementing business logic. This structure facilitates the integration of GIS files into web semantics by providing a robust framework for data manipulation, storage, and retrieval, all while ensuring the application remains scalable, maintainable, and secure.

# Code Explanation

## `ResController` Documentation

The `ResController` class is a Spring Boot REST controller part of the project. This controller is responsible for handling web requests related to resource management, including file storage, SPARQL queries, and triplestore updates. Below is the documentation of its functionalities, endpoints, and usage.

### Overview

- **Package:** `info.ponciano.lab.spalodwfs.controller`
- **Dependencies:** Spring Web, Jena RDF library, JSON processing libraries
- **Main Responsibilities:**
  - Signing in users and saving form data.
  - Executing SPARQL SELECT queries against a configured triplestore.
  - Performing updates (add, modify, delete) on RDF triples.
  - Uplifting GeoJSON files to RDF and updating ontology.

### Configuration Properties

The controller uses several configuration properties, defined in the `application.properties` file, to configure the triplestore endpoints and application URLs.

- `spring.application.VITE_APP_GRAPH_DB`: URL of the graph database.
- `spring.application.VITE_APP_API_BASE_URL`: Base URL for the API.
- `spring.application.VITE_APP_FRONT_BASE_URL`: Base URL for the frontend application.
- `spring.application.GRAPHDB_QUERY_ENDPOINT`: SPARQL query endpoint.
- `spring.application.GRAPHDB_UPDATE_ENDPOINT`: SPARQL update endpoint.

### Endpoints

#### POST `/api/sign-in`

Allows users to sign in by submitting form data.

- **Request Body:** `FormData` object containing user credentials or other form data.
- **Response:** HTTP 200 OK on successful data submission.

#### POST `/api/sparql-select`

Executes a SPARQL SELECT query against the configured triplestore and returns the results.

- **Request Body:** `SparqlQuery` object containing the SPARQL query string and the target triplestore URL.
- **Response:** A string representation of the query results, typically in JSON format.

#### POST `/api/update`

Updates the triplestore with a given RDF triple operation (add, delete).

- **Request Body:** `TripleOperation` object specifying the operation (add or delete) and the triple data (`subject`, `predicate`, `object`).
- **Response:** HTTP 200 OK on successful update.

#### POST `/api/uplift`

Converts a GeoJSON file to RDF format and updates the ontology with the uplifted data.

- **Parameters:** `file` (multipart file) containing the GeoJSON data to be uplifted.
- **Response:** A string indicating the path to the updated ontology file or an error message.

### Usage

#### Signing In

To sign in a user, send a POST request to `/api/sign-in` with form data in the request body. The data is saved and processed by the server.

#### Executing SPARQL Queries

To execute a SPARQL SELECT query, send a POST request to `/api/sparql-select` with a `SparqlQuery` object in the request body. The query results are returned as a string.

#### Updating Triplestore

To add or delete RDF triples, send a POST request to `/api/update` with a `TripleOperation` object specifying the operation and triple data.

#### Uplifting GeoJSON to RDF

To uplift a GeoJSON file and update the ontology, send a POST request to `/api/uplift` with the file as multipart form data. The path to the updated ontology file is returned.

### Error Handling

The controller includes error handling for invalid requests, such as unsupported operations or file processing issues. Exceptions are thrown for invalid operations or file issues, with appropriate HTTP response codes returned to the client.

### Conclusion

The `ResController` class in the SPALODWFS project provides a comprehensive interface for interacting with RDF data and ontologies through web requests. It supports user sign-in, SPARQL query execution, RDF triple updates, and the conversion of GeoJSON to RDF, facilitating the integration of semantic web technologies with file storage solutions.

## Documentation for `OGCAPIController` in the SPALOD Project

The `OGCAPIController` class is part of the SPALODWFS project, aimed at managing and providing access to geospatial datasets and collections through a web API following the OGC (Open Geospatial Consortium) API standards. This controller facilitates interactions with the underlying triplestore and delivers content in various formats, including HTML and JSON, catering to different client needs.

### Overview

`OGCAPIController` is a Spring Boot controller that routes HTTP requests to specific handler methods. It is annotated with `@RestController` and `@RequestMapping("/api/spalodWFS")`, indicating it handles HTTP requests for the SPALODWFS API.

### Key Endpoints

#### Landing Page

- **Endpoint**: `GET /`
- **Description**: Returns a JSON object representing the API's landing page, providing links to the API's conformance and collections endpoints in both HTML and JSON formats.

#### Collections List

- **Endpoint**: `GET /collections`
- **Description**: Fetches and returns a list of all collections available in the triplestore, including their names and identifiers.

#### Collection Query

- **Endpoint**: `GET /collections/{collectionId}`
- **Description**: Returns details about a specific collection identified by `collectionId`, including its title, description, publisher, and associated datasets.

#### Dataset List

- **Endpoint**: `GET /collections/{collectionId}/items`
- **Description**: Lists all datasets within a specific collection, including details like title, description, publisher, and distribution.

#### Dataset Items

- **Endpoint**: `GET /collections/{collectionId}/items/{datasetId}`
- **Parameters**:
  - `bbox` (optional): Bounding box to filter the items.
  - `datetime` (optional): Date and time to filter the items.
- **Description**: Returns items from a specific dataset within a collection, optionally filtered by bounding box and datetime. The response includes a path to a JSON file containing the requested data.

#### Conformance

- **Endpoint**: `GET /conformance`
- **Description**: Provides information about the API's conformance to OGC standards, listing the supported OGC API Records 1.0 conformance classes.

### How to Use

To interact with the `OGCAPIController`, clients can send HTTP GET requests to the specified endpoints. For example, to request the list of collections:

```bash
curl http://localhost:8080/api/spalodWFS/collections
```

To query a specific collection:

```bash
curl http://localhost:8080/api/spalodWFS/collections/{collectionId}
```

Replace `{collectionId}` with the actual ID of the collection you wish to query.

### Error Handling

While the provided code snippet does not explicitly include error handling mechanisms, it's crucial to implement proper error handling to manage issues like invalid collection IDs, unavailable datasets, or server errors. This could involve returning appropriate HTTP status codes and error messages to the client.

### Conclusion

The `OGCAPIController` serves as a central component in the SPALODWFS project, enabling access to geospatial data through a web API compliant with OGC standards. By following the provided documentation, developers can understand how to interact with the API, access geospatial collections and datasets, and leverage the API's capabilities in their applications.

## Documentation for `WebSecurityConfig` in the SPALOD Project

This `WebSecurityConfig` class is a configuration class for Spring Security, which is used to customize security settings for a Spring Boot application. It extends `WebSecurityConfigurerAdapter`, providing a convenient way to set up default security configurations by overriding specific methods. Below, I will explain the key components and configurations defined in this class:

1. **`@Configuration` and `@EnableWebSecurity` Annotations:**
   - `@Configuration`: Indicates that the class is a source of bean definitions for the application context.
   - `@EnableWebSecurity`: Enables Spring Security's web security support and provides the Spring MVC integration. It also extends the `WebSecurityConfigurerAdapter`.

2. **Bean Definitions:**
   - **`passwordEncoder` Bean:** A bean for `PasswordEncoder` that is defined to use the `BCryptPasswordEncoder` with a strength of 10. This encoder is used to hash passwords before storing them in the database.
   - **`authenticationProvider` Bean:** Configures a `DaoAuthenticationProvider` with the custom `UserDetailsService` and the `passwordEncoder` to authenticate users based on the stored user details and passwords.

3. **AuthenticationManagerBuilder Configuration:**
   - This method is overridden to register the custom `DaoAuthenticationProvider` with the `AuthenticationManagerBuilder`. This allows the application to authenticate users using the provided `UserDetailsService` and `passwordEncoder`.

4. **HttpSecurity Configuration:**
   - The `configure(HttpSecurity http)` method is overridden to define custom web security configurations.
     - **URL Authorization Rules:** Configures antMatchers to define specific URL patterns and the roles required to access them. For example, `/admin` requires the "ADMIN" role, and `/user` requires the "USER" role, while `/register` is accessible to everyone.
     - **Exception Handling:** Configures an `authenticationEntryPoint` to handle authentication entry point exceptions.
     - **Form Login:** Configures form-based authentication with custom parameters for username and password, and custom success and failure handlers.
     - **CSRF and CORS:** Disables CSRF (Cross-Site Request Forgery) protection and enables CORS (Cross-Origin Resource Sharing).
     - **OAuth2 Login:** Configures OAuth2 login with a default success URL.

5. **Custom Beans and Autowired Components:**
   - **`UserService` (`userDetailsService`):** A custom `UserDetailsService` implementation for loading user-specific data.
   - **`PasswordEncoder` (`passwordEncoder`):** The `PasswordEncoder` bean used for encoding passwords.
   - **`EntryPoint` (`authenticationEntryPoint`):** A custom entry point to handle authentication errors.

This class provides a comprehensive setup for securing a Spring Boot application, including user authentication (both form-based and OAuth2), authorization based on roles, and custom handling of authentication success and failure. It showcases how to integrate Spring Security with custom user details service and password encoding, manage security exceptions, and configure security settings specific to the application's needs.

## Documentation for `FileSystemStorageService` in the SPALOD Project
The `LoginController` class in the provided code snippet is a Spring Boot controller designed for managing user authentication and authorization, along with specific functionalities related to user roles and external API interactions (in this case, with GitHub). It demonstrates the use of Spring Security for role-based access control, OAuth2 for authentication, and the `RestTemplate` for consuming external APIs. Here's an overview of its key components and functionalities:

1. **Annotations and Spring Dependency Injection:**
   - `@RestController`: Marks this class as a controller where every method returns a domain object instead of a view. It's shorthand for including both `@Controller` and `@ResponseBody`.
   - `@Autowired`: Automatically injects the instance of `UserService` and `OAuth2AuthorizedClientService` into the controller, which are used for user management and handling OAuth2 authorized clients, respectively.

2. **Role-Based Access Control:**
   - `@RolesAllowed`: This annotation is used to specify the security roles that are allowed to access the methods. It ensures that only authenticated users with specified roles (either "USER" or "ADMIN") can access these endpoints.

3. **Controller Endpoints:**
   - **User and Admin Endpoints:** There are specific endpoints for users (`/user`) and admins (`/admin` and `/addAdmin`), showcasing simple role-based access control. The `/addAdmin` endpoint additionally allows for the promotion of a user to an admin role.
   - **Status and UUID Endpoints:** The `/status` and `/uuid` endpoints provide functionality for all logged-in users, allowing them to check their login status and retrieve a UUID associated with their username, respectively.
   - **External API Interaction (`/getGitUser`):** This endpoint demonstrates how to interact with an external API (GitHub's user API in this case) using an OAuth2 token. It uses `OAuth2AuthenticationToken` to extract the authentication details, `OAuth2AuthorizedClientService` to obtain the access token, and `RestTemplate` with `HttpHeaders` to make the authenticated request to GitHub's API.

4. **OAuth2 Authentication and External API Request:**
   - The method `gitUser` shows a practical example of using Spring Security with OAuth2 for consuming external APIs. It retrieves the current user's OAuth2 authentication token, uses it to construct an HTTP request with authorization headers, and then sends a request to GitHub's user API to fetch user details.

5. **Error Handling and ResponseEntity:**
   - The use of `ResponseEntity` in various endpoints allows for flexible HTTP responses, enabling the controller to return both the response body and status codes. This is evident in methods like `addAdmin` and `getUserId`, where specific statuses are returned based on the method's execution.

This class is a comprehensive example of integrating Spring Security, OAuth2 authentication, role-based access control, and external API interactions in a Spring Boot application. It showcases how to protect endpoints based on user roles, how to manage user roles within the application, and how to securely interact with external services using authenticated requests.

## Documentation for `Triplestore` in the SPALOD Project
This Java class, `Triplestore`, is designed to manage interactions with a triplestore, a type of database optimized for storing and querying data modeled as triples, with each triple representing a subject-predicate-object relation. This class provides a singleton pattern to ensure only one instance is created, methods for executing SPARQL queries and updates, and utility functions for handling triples. Here's a detailed breakdown of its components and functionality:

### Class Overview

- **Singleton Pattern**: Ensures a single instance of `Triplestore` is created. This is achieved through a private static instance and a public static method `get()` to access this instance.
- **Constructor**: Initializes the dataset from a specified directory using TDB2, a component of Apache Jena for RDF storage and querying.
- **Utility Methods**:
  - `getClassName(String datatypeURI)`: Determines the Java class name corresponding to a datatype URI.
  - `escapeString(String string)`: Escapes special characters in a string to ensure it's safely included in SPARQL queries.
  - `ensureUriWithNamespace(String input)`: Ensures a given input has a namespace if it doesn't form a valid URI.
- **SPARQL Query Execution**:
  - `executeSelectQuery(String queryString)`: Executes a SELECT query locally within the dataset.
  - `executeSelectQuery(String sparqlQuery, String triplestore)`: Executes a SELECT query against a specified triplestore endpoint.
  - `executeUpdateQuery(String updateString)`: Executes an update query locally.
  - `executeUpdateQuery(String query, String graphdbUpdateEndpoint)`: Executes an update query against a GraphDB endpoint.
- **Triple Management**:
  - Methods for adding and removing triples (`addTriple` and `removeTriple`), supporting both literal and URI objects.
- **Ontology and Data Integration**:
  - `addOntology(Model model)`: Adds triples from a Jena model to the triplestore, handling datatype mappings and escaping literals.
  - `getUnknownPredicates(Model model)`: Identifies predicates in a model that are not known to the triplestore.
- **Transaction Management**: Uses dataset transactions (`dataset.begin()`, `dataset.end()`) to ensure data consistency during read and write operations.
- **ASK Queries**: Supports ASK queries to check for the existence of specific conditions in the triplestore.

### Key Features

- **Integration with Apache Jena**: Leverages Apache Jena's capabilities for RDF model management, SPARQL querying, and TDB2 for dataset operations.
- **Flexibility in Query Execution**: Allows executing queries both locally and against remote SPARQL endpoints, facilitating interaction with external triplestores.
- **Utility Functions**: Provides methods for common tasks like escaping strings for SPARQL queries and converting datatype URIs to Java class names.
- **Error Handling**: Includes basic error handling, particularly in `executeUpdateQuery` for remote updates, catching exceptions and logging them.

### Potential Enhancements

While the class provides a robust foundation for triplestore interaction, there are areas for potential enhancement:
- **Improved Error Handling**: More comprehensive error handling could be implemented, especially for network errors or SPARQL syntax errors.
- **Transaction Management**: Expanding on the transaction management to handle more complex scenarios or rollback on errors.
- **Performance Optimization**: For large datasets or intensive querying, performance optimizations such as caching query results or parallel query execution could be beneficial.
- **Security**: Adding security features, such as authentication and authorization for accessing the triplestore, especially for updates.

This class serves as a comprehensive tool for managing RDF data and interacting with triplestores, demonstrating the power of semantic web technologies in Java applications.

## Documentation for `GeoJsonRDF` in the SPALOD Project
To integrate this `GeoJsonRDF` class with the previously outlined ontology classes and manage spatial data effectively, you'll want to ensure that the class definitions, methods, and interactions are coherent and aligned with the overarching goals of your project. Here's how the `GeoJsonRDF` class can be aligned and utilized:

### Overview of `GeoJsonRDF` Class
This class is designed to:
- **Uplift GeoJSON data into RDF**: It converts GeoJSON data into an RDF format that can be managed within an ontology. This process includes creating individuals for features and geometries, assigning properties, and linking features to datasets.
- **Downlift RDF to GeoJSON**: Conversely, it can take RDF data describing spatial features and convert it back into a GeoJSON format for use in applications that consume GeoJSON.

### Integration Points
1. **Ontology Classes (`PiOnt`, `Individual`, etc.)**: The `GeoJsonRDF` class relies on these ontology classes to create, retrieve, and manipulate RDF data. It's crucial that these classes provide the necessary functionality to manage RDF triples, such as creating individuals, adding properties, and querying the ontology.
   
2. **Handling of Spatial Data**: The class uses specific properties (`GEOSPARQLHAS_GEOMETRY`, `GEOSPARQLAS_WKT`, etc.) and classes (`SF`, `GEOSPARQL_FEATURE`, etc.) from the GeoSPARQL standard. Your ontology needs to incorporate or align with GeoSPARQL to ensure compatibility.

3. **GeoJSON and Geometry Handling**: It handles both the conversion of GeoJSON into RDF and vice versa. The class must accurately interpret GeoJSON's structure, including feature collections and geometries, and translate this into an RDF representation that captures both the spatial data and any associated properties. Similarly, for downlifting, it needs to accurately convert RDF representations back into GeoJSON.

### Practical Considerations
- **GeoSPARQL Compliance**: Ensure that your ontology is compatible with or extends GeoSPARQL to utilize spatial data effectively. This includes defining the necessary classes and properties for representing geometries and their relationships.
- **Error Handling and Validation**: The class should robustly handle errors, such as invalid GeoJSON input or RDF data that does not conform to expected structures. This includes validating GeoJSON before uplifting and ensuring RDF data integrity before downlifting.
- **Performance Optimization**: Processing large GeoJSON files or complex RDF graphs can be resource-intensive. Consider implementing performance optimizations, such as batch processing or using efficient libraries for parsing and generating GeoJSON and RDF.

### Example Usage Scenario
Imagine a scenario where you need to integrate spatial data from various sources into your ontology for analysis and visualization. You could use the `GeoJsonRDF` class to uplift this data into RDF, enabling you to query and manipulate it using SPARQL alongside other data in your ontology. Later, you might downlift part of this RDF data back into GeoJSON for visualization in a web application.

### Conclusion
The `GeoJsonRDF` class serves as a bridge between the GeoJSON format widely used for representing spatial data and the RDF format used in ontologies for linked data. By carefully integrating this class with your ontology framework and considering the practical aspects of handling spatial data, you can effectively manage and utilize spatial information within your semantic web applications.

# Conclusion

In this documentation, we have embarked on an in-depth exploration of leveraging Java to perform data conversion between GeoJSON and RDF formats, highlighting the significance of these operations within the realms of geographic information systems (GIS) and the semantic web. Our journey encompassed a broad spectrum of topics, from the fundamentals of JSON handling in Java to the sophisticated nuances of ontology management and spatial data representation.

**Key Highlights:**

- **GeoJSON and RDF Integration:** We have demonstrated the process of uplifting GeoJSON to RDF, enabling GeoJSON data to be utilized within semantic web applications. This integration facilitates richer, interconnected data models that enhance the utility and analysis of geographic information.

- **Advanced Java Techniques:** The utilization of Java for parsing, file I/O operations, exception handling, and working with libraries like Apache Jena showcased Java's capability to manage complex data processing tasks effectively, making it an invaluable tool for developers in the GIS and semantic web domains.

- **Ontology Management:** The creation, manipulation, and utilization of ontologies were central to our discussion, underscoring the importance of structured knowledge representation in enabling sophisticated reasoning and inference capabilities over geographic data.

- **Data Conversion Challenges:** We navigated the intricacies involved in converting between GeoJSON and RDF, addressing the challenges related to data integrity, format differences, and the preservation of spatial information accuracy.

- **Custom Java Framework Development:** The development of a custom Java framework for facilitating the GeoJSON to RDF conversion process exemplifies the adaptability and power of Java in addressing specific data conversion needs, providing a template for similar endeavors.

- **Practical Implications:** The practical applications of the discussed technologies span across various fields such as urban planning, environmental monitoring, and more, showcasing the transformative potential of integrating GeoJSON with RDF to support advanced data analysis and decision-making processes.

**Final Thoughts:**

The comprehensive exploration provided in this documentation illustrates the complexity and the potential of combining GeoJSON and RDF within the context of Java programming. By bridging these formats, we unlock new possibilities for data representation, sharing, and analysis across a myriad of applications. The challenges encountered and overcome serve as a testament to the evolving landscape of data processing and the continuous need for innovative solutions to harness the full potential of geographic information in the digital age.

As we conclude, it is clear that the journey from GeoJSON to RDF and beyond is not merely a technical endeavor but a gateway to realizing the vast possibilities of the semantic web and GIS technologies. This documentation serves as a foundation for future exploration and development, encouraging further innovation and exploration in the rich intersection of geographic information systems, semantic web technologies, and Java programming.
## Example of request:
```
SELECT ?s ?p ?o WHERE {?s ?p ?o. ?d rdf:type dcat:Dataset . ?d spalod:hasFeature ?s}

SELECT ?d ?item ?coords WHERE { ?d rdf:type dcat:Dataset . ?d spalod:hasFeature ?f. ?f geosparql:hasGeometry ?g. ?g geosparql:asWKT ?coords . ?f spalod:itemlabel ?item}
SELECT ?d ?item ?coordinates WHERE { ?d rdf:type dcat:Dataset . ?d spalod:hasFeature ?f. ?f geosparql:hasGeometry ?g. ?g geosparql:asWKT ?coordinates . ?f spalod:itemlabel ?item}
SELECT ?d ?itemID ?itemLabel ?coordinates WHERE { ?d rdf:type dcat:Dataset . ?d spalod:hasFeature ?itemID. ?itemID geosparql:hasGeometry ?g. ?g geosparql:asWKT ?coordinates . ?itemID spalod:itemlabel ?itemLabel}


SELECT ?f ?g ?wkt ?fp ?o WHERE { spalod:67504af5-5d32-4815-ae53-fb879f4bb0c7 spalod:hasFeature  ?f. ?f geosparql:hasGeometry ?g. ?g geosparql:asWKT ?wkt . ?f ?fp ?o}

```

On azure:
```
/opt/apache-maven-3.9.3/bin/mvn  package
export SPRING_PROFILES_ACTIVE=prod

/usr/local/bin/spalod-run.sh 
 #!/bin/bash

# timestamp
timestamp=$(date "+%Y.%m.%d-%H.%M.%S")

# run npm
cd /home/i3mainz/SpaLod/src/main/vue_js/spalod/
echo "Running npm..."
npm run dev -- --host > /var/log/npm_$timestamp.log 2>&1 &
echo $! > /var/run/npm.pid
cat  /var/run/npm.pid
# run java jar
cd /home/i3mainz/SpaLod/
echo "Running java jar..."
/opt/apache-maven-3.9.3/bin/mvn  package  -DskipTests
java -jar target/spalod-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod > /var/log/java_$timestamp.log 2>&1 &
echo $! > /var/run/java.pid
cat /var/run/java.pid
# run graphdb
echo "Running graphdb..."
/home/i3mainz/graphdb-10.2.1/bin/graphdb > /var/log/graphdb_$timestamp.log 2>&1 &
echo $! > /var/run/graphdb.pid
cat  /var/run/graphdb.pid

/usr/local/bin/spalod-stop.sh 
#!/bin/bash

# stop npm
if [ -f /var/run/npm.pid ]; then
    echo "Stopping npm..."
    kill -9 $(cat /var/run/npm.pid)
    rm /var/run/npm.pid
fi

# stop java jar
if [ -f /var/run/java.pid ]; then
    echo "Stopping java jar..."
    kill -9 $(cat /var/run/java.pid)
    rm /var/run/java.pid
fi

# stop graphdb
if [ -f /var/run/graphdb.pid ]; then
    echo "Stopping graphdb..."
    kill -9 $(cat /var/run/graphdb.pid)
    rm /var/run/graphdb.pid
fi

/usr/local/bin/spalod-run.sh 
 #!/bin/bash

# timestamp
timestamp=$(date "+%Y.%m.%d-%H.%M.%S")

# run npm
cd /home/i3mainz/SpaLod/src/main/vue_js/spalod/
echo "Running npm..."
npm run dev -- --host > /var/log/npm_$timestamp.log 2>&1 &
echo $! > /var/run/npm.pid
cat  /var/run/npm.pid
# run java jar
cd /home/i3mainz/SpaLod/
echo "Running java jar..."
/opt/apache-maven-3.9.3/bin/mvn  package  -DskipTests
java -jar target/spalod-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod > /var/log/java_$timestamp.log 2>&1 &
echo $! > /var/run/java.pid
cat /var/run/java.pid
# run graphdb
echo "Running graphdb..."
/home/i3mainz/graphdb-10.2.1/bin/graphdb > /var/log/graphdb_$timestamp.log 2>&1 &
echo $! > /var/run/graphdb.pid
cat  /var/run/graphdb.pid
```