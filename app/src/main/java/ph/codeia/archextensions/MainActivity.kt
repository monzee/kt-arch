package ph.codeia.archextensions

import android.arch.lifecycle.ViewModel
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import ph.codeia.arch.extensions.viewModel
import ph.codeia.arch.tasks.Action
import ph.codeia.arch.tasks.execute
import ph.codeia.arch.tasks.observe
import java.util.*

class MainActivity : AppCompatActivity() {

    class State : ViewModel() {
        var count = 0
        val rng = Random()
        val slowNext = Action {
            Thread.sleep(2000)
            if (rng.nextInt(5) == 0) throw RuntimeException("random error")
            count + 1
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val my = viewModel<State>()
        val count = the_count
        val inc = do_increment
        count.text = "${my.count}"
        inc.isEnabled = true
        inc.setOnClickListener {
            my.slowNext.execute()
        }
        observe(my.slowNext) {
            onExecute {
                count.text = "sec\npls"
                inc.isEnabled = false
            }

            onSuccess { n ->
                inc.isEnabled = true
                n?.let {
                    my.count = it
                    count.text = "$it"
                }
            }

            onFailure {
                count.text = "${my.count}"
                inc.isEnabled = true
                Toast.makeText(applicationContext, it.message, Toast.LENGTH_LONG).show()
            }
        }
    }
}
