package io.sn.quaternium.plugins

import io.sn.quaternium.dictFilename
import java.nio.charset.Charset
import kotlin.test.Test

class SubjectParserTest {


    @Test
    fun `test subject parser`() {
        val content =
            this.javaClass.classLoader.getResourceAsStream(dictFilename)!!.readAllBytes().toString(Charset.defaultCharset())
        var trueCnt = 0
        var falseCnt = 0
        content.let { it ->
            it.split("\n-\n").map { raw ->
                val parser = SubjectParser(raw.split("\n"))
                if (parser.type == "判断题") {
                    parser.key[0].let { k ->
                        if (k == 0) trueCnt += 1 else falseCnt += 1
                    }
                }
            }
        }
        println("$trueCnt $falseCnt")

    }

}