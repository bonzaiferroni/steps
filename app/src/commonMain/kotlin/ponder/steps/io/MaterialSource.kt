package ponder.steps.io

import kabinet.utils.randomUuidStringId
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
import ponder.steps.model.data.QuantityType
import ponder.steps.model.data.StepMaterialId
import ponder.steps.model.data.StepMaterialJoin

class MaterialSource(
    private val stepMaterialDao: StepMaterialDao = appDb.getStepMaterialDao(),
    private val materialDao: MaterialDao = appDb.getMaterialDao(),
) {
    fun flowStepMaterialsByStepId(stepId: StepId) = stepMaterialDao.flowStepMaterialsByStepId(stepId)

    suspend fun searchMaterialsByLabel(label: String) = materialDao.searchMaterialsByLabel(label)

    suspend fun createNewMaterial(label: String, materialType: MaterialType, quantityType: QuantityType): Material? {
        val materialId = randomUuidStringId()
        val material = Material(
            id = materialId,
            label = label,
            materialType = materialType,
            quantityType = quantityType,
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
        val stepMaterialId = randomUuidStringId()
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

    suspend fun deleteStepMaterialById(stepMaterialId: StepMaterialId) = stepMaterialDao.deleteStepMaterialById(stepMaterialId)
}