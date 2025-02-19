package dev.domnikl.schema_registry_gitops.cli

import dev.domnikl.schema_registry_gitops.DaggerAppComponent
import dev.domnikl.schema_registry_gitops.state.Dumper
import dev.domnikl.schema_registry_gitops.state.Persistence
import picocli.CommandLine
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Callable
import javax.inject.Inject

@CommandLine.Command(
    name = "dump",
    description = ["prints the current state"]
)
class Dump : Callable<Int> {
    constructor() { // used by picocli
        val appComponent = DaggerAppComponent.create()

        this.persistence = appComponent.persistence()
        this.dumper = appComponent.dumper()
    }

    @Inject constructor(
        persistence: Persistence,
        dumper: Dumper
    ) {
        this.persistence = persistence
        this.dumper = dumper
    }

    private var persistence: Persistence
    private var dumper: Dumper

    @CommandLine.Parameters(description = ["optional path to output YAML file, default is \"-\", which prints to STDOUT"], defaultValue = STDOUT_FILE)
    private lateinit var outputFile: String

    private val outputStream by lazy {
        when (outputFile) {
            STDOUT_FILE -> System.out
            else -> BufferedOutputStream(FileOutputStream(File(outputFile)))
        }
    }

    override fun call(): Int {
        val state = dumper.dump()

        persistence.save(state, outputStream)

        return 0
    }

    companion object {
        private const val STDOUT_FILE = "-"
    }
}
