package io.sn.quaternium.plugins

import kotlinx.serialization.Serializable

@Serializable
sealed class Question {

    abstract val question: String
    abstract val difficulty: String

    @Serializable
    class SingleChoiceQuestion(
        override val question: String,
        override val difficulty: String,
        val options: List<String>,
        val correctAnswer: Int
    ) : Question()

    @Serializable
    class MultipleChoiceQuestion(
        override val question: String,
        override val difficulty: String,
        val options: List<String>,
        val correctAnswers: List<Int>
    ) : Question()

    @Serializable
    class TrueFalseQuestion(
        override val question: String,
        override val difficulty: String,
        val correctAnswer: Boolean
    ) : Question()
}