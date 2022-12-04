package io.github.cidverse.cid.sdk.util

import java.io.File

fun File.extensionWithDot(): String {
	val parts = this.name.split(".")
	return if (parts.size >= 2) {
		// remove the first part and join the remaining parts with a dot
		"." + parts.drop(1).joinToString(".")
	} else {
		""
	}
}
