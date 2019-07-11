package net.technearts

data class KMember(var title: String? = null,
                   var column: String? = null,
                   var converter: String? = null,
                   var property: String? = null,
                   var mappedBy: String? = null,
                   var mapped: Boolean? = true)

data class KMapping(var name: String? = null,
                    var sheet: String? = null,
                    var className: String? = null,
                    var members: List<KMember>? = null)

data class KMappings(var mappings: List<KMapping>? = null)
