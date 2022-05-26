package com.sbrl.peppermint.lib.wiki_api

class WikiVersion(inVersionString: String) : Comparable<WikiVersion> {
	/**
	 * The major version of the wiki.
	 * For example, given the version v0.24-beta2, the major version is 0.
	 */
	val major: Int
	/**
	 * The minor version of the wiki.
	 * For example, given the version v0.24-beta2, the major version is 24.
	 */
	var minor: Int
	
	/**
	 * The patch version of the wiki.
	 * For example, given the version v0.24.1-beta1, the patch version is 1.
	 * Not always present. If it is not present, this will be 0.
	 */
	var patch: Int
	/**
	 * The extra type annotation from the wiki's version.
	 * For example, given the version v0.24-beta2, the type annotation is beta2.
	 * Not always present. If it is not present, this will be an empty string.
	 */
	var type: String
	
	enum class TypeAnnotationClass { Dev, Beta, None, Hotfix }
	
	/**
	 * Returns the class of the type annotation.
	 * For example, v0.24-beta1 has a class of beta, v0.22-dev has a class of dev, etc.
	 */
	val typeClass: TypeAnnotationClass
		get() {
			if(type.startsWith("dev"))
				return TypeAnnotationClass.Dev
			if(type.startsWith("beta"))
				return TypeAnnotationClass.Beta
			if(type.startsWith("hotfix"))
				return TypeAnnotationClass.Hotfix
			return TypeAnnotationClass.None
		}
	
	init {
		// Strip the v at the front
		val versionStripped = if(inVersionString[0] == 'v')
			inVersionString.substring(1)
		else inVersionString
		// Remove the -dev / -betaX / etc
		val versionOnly = if(versionStripped.contains("-"))
			versionStripped.substring(0, versionStripped.indexOf("-"))
		else versionStripped
		
		// Divide into parts, extract major & minor
		val parts = versionOnly.split(".")
		if(parts.size < 2) throw Exception("Error: Invalid version code.")
		
		major = parts[0].toInt()
		minor = parts[1].toInt()
		patch = if(parts.size >= 3) parts[2].toInt() else 0
		
		type = if(versionStripped.contains("-"))
			versionStripped.substring(versionStripped.indexOf("-"))
		else ""
	}
	
	override fun equals(other: Any?) : Boolean {
		if (other !is WikiVersion) return false
		
		return major == other.major &&
			minor == other.minor &&
			patch == other.patch &&
			type == other.type
	}
	
	override fun compareTo(other: WikiVersion) : Int {
		if(major > other.major) return 1
		if(major < other.major) return -1
		
		if(minor > other.minor) return 1
		if(minor < other.minor) return -1
		
		if(patch > other.patch) return 1
		if(patch < other.patch) return -1
		
		if(typeClass.ordinal > other.typeClass.ordinal) return 1
		if(typeClass.ordinal < other.typeClass.ordinal) return -1
		
		return type.compareTo(other.type)
	}
	
	override fun hashCode(): Int {
		var result = major
		result = 31 * result + minor
		result = 31 * result + patch
		result = 31 * result + type.hashCode()
		return result
	}
}