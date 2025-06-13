package io.sn.quaternium.plugins

class SubjectParser(private val raw: List<String>) {

    val index: Int
        get() = raw[0].split(". ")[0].toInt()

    val difficulty: String
        get() = raw[0].split("难度：")[1]

    val type: String
        get() = raw[0].split(". ")[1].split("（")[0]

    val title: String
        get() = raw[2]

    val choices: List<String>
        get() = when (type) {
            "判断题" -> {
                listOf("A. 对", "B. 错")
            }

            "单选题", "多选题" -> {
                (3 until raw.indexOf("未作答")).map { line ->
                    val orig = raw[line]
                    Regex("""(\w)\.(.*)""").matchEntire(orig).let { rst ->
                        val note = rst!!.groupValues[1]
                        val content = rst.groupValues[2]
                        "$note. ${content.trimStart()}"
                    }
                }
            }

            else -> throw IllegalStateException("Unknown subject type: $type")
        }

    val key: List<Int>
        get() = when (type) {
            "判断题" -> {
                val orig = raw[5]
                listOf(orig.removePrefix("参考答案").let {
                    if (it == "对") 0 else 1
                })
            }

            "单选题", "多选题" -> {
                val orig = raw[raw.indexOf("未作答") + 1]
                orig.removePrefix("参考答案").let { answer ->
                    answer.split(".").filter { it.isNotEmpty() }.map { choice -> charIndexer(choice, orig) }
                }
            }

            else -> throw IllegalStateException("Unknown subject type: $type")
        }

    companion object {
        fun charIndexer(char: String, orig: String): Int {
            if (char.length == 1 && "A" <= char && char <= "Z") {
                return char[0].code - 'A'.code
            } else
                throw IllegalStateException("choice unsupported: $char, $orig")
        }

        fun charDedexer(code: Int?): String {
            if (code == null) return "空"
            return if (code in 0..25) {
                (code + 'A'.code).toChar().toString()
            } else "空"
        }
    }

}