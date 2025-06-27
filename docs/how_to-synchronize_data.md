Modify Example.kt
	Add updatedAt
	
Modify ExampleEntity.kt
	Add updatedAt
	Add updatedAt to toEntity
	
Modify ExampleTable.kt
	Add updatedAt to table
	Add updatedAt to toExample
	Add upsertExample(userId: String)

Modify AppDatabase.kt
	Bump version number
	
Modify synchronizedData.kt
	Add "example" to synchronizedData

Modify SyncData.kt
	Add example: List<Example>
	Add example to isEmpty
	
Modify DataMerger.kt
	Add "examples: ${data.examples}" to logData
	add example = resolveConflicts to resolveConflicts
	
Modify SyncDao.kt
	add readExampleUpdated()
	add upsert(ExampleEntity)
	add deleteExampleInList()
	
Modify LocalSyncRepository.kt
	Add Example retrieval to readSync
	Add Example upsert to writeSync
	Add Example deletion to writeSync
	
Modify SyncApiService.kt
	Read Examples from ExampleTable in readSync()
	batchUpsert examples in writeSync()
	deleteWhere examples in writeSync()
