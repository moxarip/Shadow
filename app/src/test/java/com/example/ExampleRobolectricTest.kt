package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.audio.GameAudioManager
import com.example.data.GameRepository
import com.example.game.engine.GameEngine
import com.example.game.model.CharacterId
import com.example.game.model.CharacterProgress
import com.example.game.model.CharacterRegistry
import com.example.game.model.WeaponRegistry
import com.example.game.model.WeaponType
import com.example.game.model.WorldRegistry
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
        assertEquals("Shadow Warriors", appName)
    }

    @Test
    fun `character registry contains 10 characters and Blade is first and free`() {
        assertEquals(10, CharacterRegistry.allCharacters.size)
        val blade = CharacterRegistry.getDef(CharacterId.BLADE)
        assertEquals("BLADE", blade.name)
        assertEquals(0, blade.unlockGemsCost)
        assertEquals(WeaponType.ENERGY_KATANA, blade.weaponType)
    }

    @Test
    fun `weapon registry contains 10 weapons`() {
        assertEquals(10, WeaponRegistry.allWeapons.size)
        val katana = WeaponRegistry.getDef(WeaponType.ENERGY_KATANA)
        assertEquals("Energy Katana", katana.name)
    }

    @Test
    fun `world registry contains 5 worlds with bosses`() {
        assertEquals(5, WorldRegistry.allWorlds.size)
        val world1 = WorldRegistry.allWorlds.first()
        assertEquals("Neon District", world1.name)
        assertNotNull(world1.bossDef)
    }

    @Test
    fun `game repository initializes with defaults`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = GameRepository(context)
        val prog = repo.progression.value
        assertEquals(CharacterId.BLADE, prog.selectedCharacterId)
        assertTrue(prog.coins >= 0)
        assertTrue(prog.gems >= 0)
        assertTrue(prog.characters[CharacterId.BLADE]?.isUnlocked == true)
    }

    @Test
    fun `game engine initializes side-scrolling stage with player and enemies`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val audioManager = GameAudioManager(context)
        val bladeDef = CharacterRegistry.getDef(CharacterId.BLADE)
        val weapon = WeaponRegistry.getDef(WeaponType.ENERGY_KATANA)
        val world = WorldRegistry.allWorlds.first()
        val engine = GameEngine(
            characterDef = bladeDef,
            characterProgress = CharacterProgress(CharacterId.BLADE, 1, true),
            weaponDef = weapon,
            worldDef = world,
            stageNumber = 1,
            audioManager = audioManager
        )

        assertNotNull(engine.player)
        assertTrue(engine.player.isAlive())
        assertTrue(engine.enemies.any { it.active })
        assertEquals(2800f, engine.stageLength)
    }
}
