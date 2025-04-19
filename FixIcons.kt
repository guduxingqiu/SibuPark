import java.io.File

fun main() {
    // 定义项目源代码路径
    val sourcePath = "app/src/main/java"
    val sourcedir = File(sourcePath)
    
    if (!sourcedir.exists() || !sourcedir.isDirectory) {
        println("源代码路径不存在或不是目录: $sourcePath")
        return
    }
    
    // 递归查找所有Kotlin文件
    val kotlinFiles = sourcedir.walkTopDown().filter { it.isFile && it.name.endsWith(".kt") }
    
    // 计数器
    var filesWithImports = 0
    var filesWithUsages = 0
    var totalFilesModified = 0
    
    // 处理每个文件
    kotlinFiles.forEach { file ->
        var content = file.readText()
        var modified = false
        
        // 1. 修复导入语句
        if (content.contains("import androidx.compose.material.icons.filled.ArrowBack")) {
            content = content.replace(
                "import androidx.compose.material.icons.filled.ArrowBack",
                "import androidx.compose.material.icons.automirrored.filled.ArrowBack"
            )
            filesWithImports++
            modified = true
        }
        
        // 2. 修复使用语句
        if (content.contains("Icons.Default.ArrowBack")) {
            content = content.replace(
                "Icons.Default.ArrowBack",
                "Icons.AutoMirrored.Filled.ArrowBack"
            )
            filesWithUsages++
            modified = true
        }
        
        // 保存修改后的文件
        if (modified) {
            file.writeText(content)
            totalFilesModified++
            println("已修复文件: ${file.absolutePath}")
        }
    }
    
    println("总计处理:")
    println("- 修复导入语句的文件: $filesWithImports")
    println("- 修复使用语句的文件: $filesWithUsages")
    println("- 总修改文件数: $totalFilesModified")
} 