Add a new data type (Example)

Create Example.kt
	Add data class
	Add Id typealias ExampleId
	Annotate @Serializable

Create ExampleEntity.kt
	Add data class
	Annotate @PrimaryKey
	Annotate @Entity
	Add foreignKeys
	Add indices
	Add toEntity
	
Create ExampleDao.kt
	Add interface
	Annotate @Dao
	Add Insert, Upsert, Update, Delete

Modify AppDatabase.kt
	Add to entities array
	Bump version
	Add getExampleDao
	
Create LocalExampleRepository.kt
	Inject Dao appDb.getExampleDao()
	
## Add to UI

## Serverside

Create ExampleTable.kt object
	Add ExampleTable object
	Add upsertExample function
	Add ResultRow.toExample() function
	
Modify Databases.kt
	Add ExampleTable to dbTables
	
Modify Api.kt
	
Create ExampleApiService.kt
