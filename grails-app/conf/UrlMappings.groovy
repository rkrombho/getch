class UrlMappings {

	static mappings = {
        /*
        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }
        */
        "/encrypt/$value"(controller:'encryption')
        "/$key"(controller:'query')
        "/"(view:"/index")
        "500"(view:'/error')
	}
}
