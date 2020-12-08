package io.glnt.gpms.common.utils

import io.github.jhipster.config.JHipsterConstants
import org.springframework.core.env.Environment

class DefaultProfileUtil {

    private constructor()

    companion object {

        private val SPRING_PROFILE_DEFAULT = "spring.profiles.default"

        /**
         * Set a default to use when no profile is configured.
         *
         * @param app the Spring application
         */
        fun addDefaultProfile(): Map<String, Any>? {
            val defProperties = HashMap<String, String>()
            /*
            * The default profile to use when no other profiles are defined
            * This cannot be set in the <code>application.yml</code> file.
            * See https://github.com/spring-projects/spring-boot/issues/1219
            */
            defProperties[SPRING_PROFILE_DEFAULT] = JHipsterConstants.SPRING_PROFILE_DEVELOPMENT
            return defProperties
        }

        /**
         * Get the profiles that are applied else get default profiles.
         *
         * @param env spring environment
         * @return profiles
         */
        fun getActiveProfiles(env: Environment): Array<String> {
            val profiles = env.activeProfiles
            if (profiles.isEmpty()) {
                return env.defaultProfiles
            }
            return profiles
        }
    }
}