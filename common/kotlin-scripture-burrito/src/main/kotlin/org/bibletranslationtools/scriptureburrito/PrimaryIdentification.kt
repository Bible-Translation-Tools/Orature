package org.bibletranslationtools.scriptureburrito

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.JsonNode


@JsonInclude(JsonInclude.Include.NON_NULL)
/**
 * Due to Primary being json objects of arbitrary schema, use a JsonNode
 */
class PrimaryIdentification: HashMap<String, JsonNode>()
