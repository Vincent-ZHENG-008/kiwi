dagger.#Plan & {
	client: {
		filesystem: "./build": write: contents: actions.build.output
		env: {
			NETLIFY_TOKEN: dagger.#Secret
			USER:          string
		}
	}

	actions: {
		pull: docker.#Pull & {
			source: "docker.mcmcnet.cn/builder/maven:3.8.4-openjdk-17"
		}
		build: {
			run: bash.#Run & {
				"mvn clean package"
			}
		}
	}
}