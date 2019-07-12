package net.technearts

import org.apache.commons.lang3.StringUtils.isNotBlank
class Member {

    var title: String? = null
    var column: String? = null
    var converterName: String? = null
    var converter: ((String) -> Any)? = null
    var property: String? = null
    var mappedBy: String? = null
    var isMapped = true

    val isTitleBased: Boolean
        get() = isNotBlank(title)

    val isReferenceBased: Boolean
        get() = isNotBlank(title) || isNotBlank(column)
}
