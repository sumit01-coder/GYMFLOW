package com.gymflow.feature.member.exercises

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CategoryFormGuideTest {

    @Test
    fun testAllMajorCategoriesHaveFormGuides() {
        val requiredCategories = listOf("CHEST", "BACK", "BICEPS", "TRICEPS", "SHOULDERS", "LEGS", "ABS", "STRETCHING")
        
        for (categoryKey in requiredCategories) {
            val guide = ExerciseVisuals.getCategoryFormGuide(categoryKey)
            assertNotNull("Form guide must exist for category $categoryKey", guide)
            assertTrue("Form GIF URL must not be blank for $categoryKey", guide!!.formGifUrl.isNotBlank())
            assertTrue("Muscle badge must not be blank for $categoryKey", guide.muscleBadge.isNotBlank())
            assertTrue("Target muscle label must not be blank for $categoryKey", guide.targetMuscleLabel.isNotBlank())
        }
    }

    @Test
    fun testCategoryFormGuideRetrievalByNameOrKey() {
        val chestByKey = ExerciseVisuals.getCategoryFormGuide("CHEST")
        val chestByName = ExerciseVisuals.getCategoryFormGuide("Chest")
        assertNotNull(chestByKey)
        assertNotNull(chestByName)
        assertEquals(chestByKey?.formGifUrl, chestByName?.formGifUrl)
        assertEquals("PECS", chestByKey?.muscleBadge)
    }
}
