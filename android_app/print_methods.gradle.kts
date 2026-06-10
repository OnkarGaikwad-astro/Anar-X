tasks.register("printTfliteMethods") {
    doLast {
        val config = configurations.detachedConfiguration(
            dependencies.create("org.tensorflow:tensorflow-lite:2.10.0")
        )
        val files = config.resolve()
        // Extract classes.jar from the AAR
        files.forEach { file ->
            if (file.name.endsWith(".aar")) {
                val zipFile = java.util.zip.ZipFile(file)
                val entry = zipFile.getEntry("classes.jar")
                if (entry != null) {
                    val stream = zipFile.getInputStream(entry)
                    val target = project.file("build/tmp/tflite-classes.jar")
                    target.parentFile.mkdirs()
                    target.outputStream().use { out ->
                        stream.copyTo(out)
                    }
                    
                    // Now read the jar using URLClassLoader
                    val cl = java.net.URLClassLoader(arrayOf(target.toURI().toURL()))
                    try {
                        val clazz = cl.loadClass("org.tensorflow.lite.Interpreter")
                        println("--- METHODS FOR org.tensorflow.lite.Interpreter ---")
                        clazz.methods.forEach { m ->
                            println("${m.name}(${m.parameterTypes.joinToString { it.name }})")
                        }
                    } catch(e: Exception) {
                        println("Class not found: $e")
                    }
                }
            }
        }
    }
}
