package io.petros.movies.movies.navigator

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.petros.movies.movies.MoviesActivity
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import strikt.api.expect
import strikt.assertions.isEqualTo

@RunWith(RobolectricTestRunner::class)
class MoviesActivityLauncherRobolectricTest {

    private val slot = slot<Intent>()

    private var appCompatActivityMock = mockk<AppCompatActivity>()
    private val testedClass = MoviesActivityLauncher(appCompatActivityMock)

    @Test
    fun `when launch is called, then current activity starts target movies activity`() {
        testedClass.launch()

        verify { appCompatActivityMock.startActivity(capture(slot)) }
        expect { that(slot.captured.component?.className).isEqualTo(MoviesActivity::class.java.name) }
    }

}
