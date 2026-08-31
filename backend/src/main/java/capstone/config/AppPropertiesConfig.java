package capstone.config;

import capstone.support.userprofile.UserProfileProperties;
import capstone.voicereport.async.VoiceReportAsyncProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({UserProfileProperties.class, VoiceReportAsyncProperties.class})
public class AppPropertiesConfig {}
