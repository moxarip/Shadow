package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.audio.GameAudioManager
import com.example.data.GameRepository
import com.example.game.engine.GameEngine
import com.example.game.model.ArenaRegistry
import com.example.game.model.GameMode
import com.example.game.model.HeroId
import com.example.game.model.HeroProgress
import com.example.game.model.HeroRegistry
import com.example.game.model.Vector2
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Shadow Clash", appName)
    }

    @Test
    fun `hero registry contains 10 heroes and Blaze is first`() {
        assertEquals(10, HeroRegistry.allHeroes.size)
        val blaze = HeroRegistry.getDef(HeroId.BLAZE)
        assertEquals("BLAZE", blaze.name)
        assertEquals(0, blaze.unlockGemsCost)
    }

    @Test
    fun `game repository initializes with defaults`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = GameRepository(context)
        val prog = repo.progression.value
        assertEquals(HeroId.BLAZE, prog.selectedHeroId)
        assertTrue(prog.coins >= 0)
        assertTrue(prog.gems >= 0)
        assertTrue(prog.heroes[HeroId.BLAZE]?.isUnlocked == true)
    }

    @Test
    fun `game engine initializes with hero and spawns wave`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val audioManager = GameAudioManager(context)
        val blazeDef = HeroRegistry.getDef(HeroId.BLAZE)
        val arena = ArenaRegistry.allArenas.first()
        val engine = GameEngine(
            heroDef = blazeDef,
            heroProgress = HeroProgress(HeroId.BLAZE, 1, true),
            arenaDef = arena,
            gameMode = GameMode.STORY,
            audioManager = audioManager
        )

        assertNotNull(engine.player)
        assertTrue(engine.enemies.isNotEmpty())
        assertEquals(1, engine.currentWave)
    }
}
