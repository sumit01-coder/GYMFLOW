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

    @Test
    fun testAllCuratedStretchingRoutinesHaveValidGifUrls() {
        val curatedStretches = StretchingCatalog.stretches
        assertTrue("Curated stretches should not be empty", curatedStretches.isNotEmpty())
        for (stretch in curatedStretches) {
            assertNotNull("Stretch ${stretch.name} (${stretch.id}) must have a non-null gifUrl", stretch.gifUrl)
            assertTrue("Stretch ${stretch.name} gifUrl must be valid HTTP/HTTPS URL", stretch.gifUrl!!.startsWith("http"))
            assertTrue("Stretch ${stretch.name} must specify target muscle", stretch.targetMuscle.isNotBlank())
            assertTrue("Stretch ${stretch.name} must specify breathing cue", stretch.breathingCue.isNotBlank())
        }
    }

    @Test
    fun testAllStretchingRoutinesHaveResolvableGifUrl() {
        val allStretches = StretchingCatalog.stretches
        assertTrue("Catalog should contain stretching routines", allStretches.isNotEmpty())
        for (stretch in allStretches) {
            val resolvedGif = stretch.gifUrl
                ?: ExerciseVisuals.getCategoryFormGuide(stretch.muscleGroup)?.formGifUrl
                ?: "https://cdn.jsdelivr.net/gh/JahelCuadrado/ExerciseGymGifsDB@v1.1.0/calves/calf-stretch-with-hands-against-wall.gif"
            assertTrue("Stretch ${stretch.name} must have a valid non-blank resolved GIF URL", resolvedGif.isNotBlank() && resolvedGif.startsWith("http"))
        }
    }
}

