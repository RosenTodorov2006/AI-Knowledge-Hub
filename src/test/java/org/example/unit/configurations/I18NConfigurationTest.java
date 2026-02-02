package org.example.unit.configurations;

import org.example.configurations.I18NConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import java.util.Locale;
import static org.junit.jupiter.api.Assertions.*;

public class I18NConfigurationTest {
    private I18NConfiguration i18nConfiguration;
    @BeforeEach
    void setUp() {
        i18nConfiguration = new I18NConfiguration();
    }
    @Test
    void testLocaleResolverBean() {
        LocaleResolver resolver = i18nConfiguration.localeResolver();
        assertNotNull(resolver);
        assertTrue(resolver instanceof CookieLocaleResolver);
    }
    @Test
    void testLocaleChangeInterceptorBean() {
        LocaleChangeInterceptor interceptor = i18nConfiguration.localeChangeInterceptor();
        assertNotNull(interceptor);
        assertEquals("lang", interceptor.getParamName());
    }
    @Test
    void testMessageSourceBean() {
        MessageSource messageSource = i18nConfiguration.messageSource();
        assertTrue(messageSource instanceof ReloadableResourceBundleMessageSource);

        ReloadableResourceBundleMessageSource reloadableSource = (ReloadableResourceBundleMessageSource) messageSource;

        String encoding = (String) ReflectionTestUtils.getField(reloadableSource, "defaultEncoding");
        assertEquals("UTF-8", encoding);

        Locale defaultLocale = (Locale) ReflectionTestUtils.getField(reloadableSource, "defaultLocale");
        assertEquals(Locale.ENGLISH, defaultLocale);
    }
}