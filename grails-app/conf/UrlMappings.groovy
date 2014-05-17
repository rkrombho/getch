class UrlMappings {

	static mappings = {
        /*
        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }
        */
        "/encrypt"(controller:'encryption')
        "/list"(controller:'query', action:'list')
        "/$key"(controller:'query')
        //"500"(view:'/error')
	}
}
