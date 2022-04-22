package com.example.stayconnected.sample.util

import androidx.recyclerview.widget.DiffUtil
import com.example.stayconnected.sample.modele.Contact

class SpotDiffCallback(
    private val old: List<Contact>,
    private val aNew: List<Contact>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return old.size
    }

    override fun getNewListSize(): Int {
        return aNew.size
    }

    override fun areItemsTheSame(oldPosition: Int, newPosition: Int): Boolean {
        return old[oldPosition].id == aNew[newPosition].id
    }

    override fun areContentsTheSame(oldPosition: Int, newPosition: Int): Boolean {
        return old[oldPosition] == aNew[newPosition]
    }

}
