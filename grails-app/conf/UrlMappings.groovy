class UrlMappings {

	static mappings = {
        /*
        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }
        */
        "/$key"(controller:'query')
        "/"(view:"/index")
        "500"(view:'/error')
	}
}
