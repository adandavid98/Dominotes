package com.example.data.domino

enum class TilePlacement {
    LEFT,
    RIGHT
}

data class DominoTile(
    val left: Int,
    val right: Int,
    val id: Int = left * 10 + right
) {
    val isDouble: Boolean get() = left == right
    val totalPoints: Int get() = left + right

    fun canMatch(pip: Int): Boolean = left == pip || right == pip

    fun orientedForMatch(targetPip: Int): DominoTile {
        return if (right == targetPip) {
            this
        } else if (left == targetPip) {
            DominoTile(right, left, id)
        } else {
            this
        }
    }

    companion object {
        fun createDoubleSixSet(): List<DominoTile> {
            val tiles = mutableListOf<DominoTile>()
            for (i in 0..6) {
                for (j in i..6) {
                    tiles.add(DominoTile(i, j))
                }
            }
            return tiles
        }
    }
}

data class PlacedTile(
    val tile: DominoTile,
    val isHorizontal: Boolean = true
)
