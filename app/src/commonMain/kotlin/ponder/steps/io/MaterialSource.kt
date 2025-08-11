package ponder.steps.io

import kabinet.utils.generateUuidString
import kotlinx.datetime.Clock
import ponder.steps.appDb
import ponder.steps.db.MaterialDao
import ponder.steps.db.StepId
import ponder.steps.db.StepMaterialDao
import ponder.steps.db.toEntity
import ponder.steps.model.data.Material
import ponder.steps.model.data.MaterialId
import ponder.steps.model.data.MaterialType
import ponder.steps.model.data.MaterialUnit
import ponder.steps.model.data.StepMaterial
import ponder.steps.model.data.UnitType
import ponder.steps.model.data.StepMaterialId

class MaterialSource(
    private val stepMaterialDao: StepMaterialDao = appDb.getStepMaterialDao(),
    private val materialDao: MaterialDao = appDb.getMaterialDao(),
) {
    fun flowStepMaterialsByStepId(stepId: StepId) = stepMaterialDao.flowStepMaterialsByStepId(stepId)

    suspend fun searchMaterials(label: String, materialType: MaterialType) =
        materialDao.searchMaterials(label, materialType)

    suspend fun createNewMaterial(label: String, materialType: MaterialType, unitType: UnitType): Material? {
        val materialId = generateUuidString()
        val material = Material(
            id = materialId,
            label = label,
            materialType = materialType,
            unitType = unitType,
            updatedAt = Clock.System.now()
        )
        val isSuccess = materialDao.insert(material.toEntity()) != -1L
        return if (isSuccess) material else null
    }

    suspend fun createNewStepMaterial(
        materialId: MaterialId,
        stepId: StepId,
        quantity: Float,
        materialUnit: MaterialUnit,
    ): StepMaterial? {
        val stepMaterialId = generateUuidString()
        val stepMaterial = StepMaterial(
            id = stepMaterialId,
            materialId = materialId,
            stepId = stepId,
            quantity = quantity,
            materialUnit = materialUnit,
            updatedAt = Clock.System.now()
        )
        val isSuccess = stepMaterialDao.insert(stepMaterial.toEntity()) != -1L
        return if (isSuccess) stepMaterial else null
    }

    suspend fun deleteStepMaterialById(stepMaterialId: StepMaterialId) =
        stepMaterialDao.deleteStepMaterialById(stepMaterialId)

    suspend fun updateStepMaterialQuantity(stepMaterialId: StepMaterialId, quantity: Float) =
        stepMaterialDao.updateStepMaterialQuantity(stepMaterialId, quantity)
}