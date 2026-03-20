# Recipe Finder

Recipe Finder is an application for discovering recipes, saving favorite dishes, generating a daily menu, and creating a shopping list from recipe ingredients.

The project combines:
- Vaadin user interface
- REST API
- database persistence
- external recipe and nutrition data sources

## Application Description

Recipe Finder allows the user to:
- browse recipes in the main dashboard
- generate a daily menu with breakfast, lunch, and dinner
- fetch random recipes from external APIs
- open a separate details page for a selected recipe
- save recipes to favorites
- keep favorites even after removing recipes from the dashboard
- add all ingredients from a recipe to the shopping list
- manage shopping items, units, bought status, and merged quantities

## Main Views

The application contains these main screens:
- `Recipe Finder Dashboard`
- `My Favorites`
- `Shopping List`
- `Recipe Details`

## REST API

The backend exposes REST endpoints for the main application areas, including:
- recipes
- favorites
- shopping list
- audit logs

Example endpoint groups:
- `/recipes`
- `/favorites`
- `/shopping`
- `/audit`

## Technologies

- Java 17
- Spring Boot
- Spring Web
- Spring Data JPA
- Vaadin
- Gradle

## Running the Project

Requirements:
- Java 17 or newer

Start the application:

```bash
./gradlew bootRun
```

On Windows:

```bash
gradlew.bat bootRun
```

After startup, open:
- `http://localhost:8080`


## Project Structure Note

This repository contains the full Recipe Finder application. The Vaadin frontend is built into the same Spring Boot project as the REST API.
