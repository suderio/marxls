package net.technearts

class Mapping {
    var name: String? = null
    var sheet: String? = null
    var className: String? = null
    var members: List<Member>? = null

    fun getMember(titleOrColumn: String): Member? {
        return members?.stream()?.filter { member ->
            if (member.isTitleBased)
                titleOrColumn == member.title
            else
                titleOrColumn == member.column
        }?.findFirst()?.orElse(null)
    }
}
