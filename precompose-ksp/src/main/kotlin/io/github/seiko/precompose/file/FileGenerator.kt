package io.github.seiko.precompose.file

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.ksp.writeTo

internal class FileGenerator(
    private val codeGenerator: CodeGenerator,
) {
    fun createNewFile(
        fileSpec: FileSpec,
        dependencies: Dependencies,
    ) {
        fileSpec.writeTo(codeGenerator, dependencies)
    }
}
