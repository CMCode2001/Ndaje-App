package com.ndajee.userservice.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests unitaires du EncryptionConverter.
 * Vérifie que le chiffrement/déchiffrement AES fonctionne correctement
 * et que les données sensibles ne sont jamais stockées en clair.
 */
@DisplayName("EncryptionConverter - Tests unitaires")
class EncryptionConverterTest {

    private EncryptionConverter converter;

    @BeforeEach
    void setUp() {
        // Clé de test de 16 caractères pour AES-128
        converter = new EncryptionConverter("test-key-12345!!");
    }

    @Test
    @DisplayName("convertToDatabaseColumn - Chiffre la valeur en Base64 non-lisible")
    void givenPlainText_whenConvertToDb_thenReturnsCipherText() {
        // Arrange
        String plainText = "john.doe@example.com";

        // Act
        String encrypted = converter.convertToDatabaseColumn(plainText);

        // Assert: la valeur chiffrée est différente du texte original
        assertThat(encrypted).isNotEqualTo(plainText);
        // La valeur est en Base64 (ne contient pas '@')
        assertThat(encrypted).doesNotContain("@");
    }

    @Test
    @DisplayName("convertToEntityAttribute - Déchiffre correctement la valeur chiffrée")
    void givenCipherText_whenConvertToEntity_thenReturnsOriginalValue() {
        // Arrange
        String original = "john.doe@example.com";
        String encrypted = converter.convertToDatabaseColumn(original);

        // Act
        String decrypted = converter.convertToEntityAttribute(encrypted);

        // Assert
        assertThat(decrypted).isEqualTo(original);
    }

    @Test
    @DisplayName("Cycle complet chiffrement/déchiffrement pour email")
    void roundTrip_email_isConsistent() {
        String email = "user.test+tag@domain.co.sn";
        assertThat(converter.convertToEntityAttribute(converter.convertToDatabaseColumn(email))).isEqualTo(email);
    }

    @Test
    @DisplayName("Cycle complet chiffrement/déchiffrement pour numéro de téléphone")
    void roundTrip_phone_isConsistent() {
        String phone = "+221781234567";
        assertThat(converter.convertToEntityAttribute(converter.convertToDatabaseColumn(phone))).isEqualTo(phone);
    }

    @Test
    @DisplayName("Cycle complet chiffrement/déchiffrement pour numéro de permis")
    void roundTrip_license_isConsistent() {
        String license = "SN-DK-2023-001234";
        assertThat(converter.convertToEntityAttribute(converter.convertToDatabaseColumn(license))).isEqualTo(license);
    }

    @Test
    @DisplayName("Cycle complet chiffrement/déchiffrement pour plaque d'immatriculation")
    void roundTrip_licensePlate_isConsistent() {
        String plate = "DK-7412-A";
        assertThat(converter.convertToEntityAttribute(converter.convertToDatabaseColumn(plate))).isEqualTo(plate);
    }

    @Test
    @DisplayName("convertToDatabaseColumn - Valeur null retourne null")
    void givenNullInput_whenConvertToDb_thenReturnsNull() {
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
    }

    @Test
    @DisplayName("convertToEntityAttribute - Valeur null retourne null")
    void givenNullDbData_whenConvertToEntity_thenReturnsNull() {
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }

    @Test
    @DisplayName("Deux chiffrements du même texte produisent la même valeur (AES ECB)")
    void givenSamePlainText_whenEncryptedTwice_thenProducesSameCipherText() {
        String text = "same-text@test.com";
        String enc1 = converter.convertToDatabaseColumn(text);
        String enc2 = converter.convertToDatabaseColumn(text);
        assertThat(enc1).isEqualTo(enc2);
    }

    @Test
    @DisplayName("Données corrompues en base lèvent une RuntimeException au déchiffrement")
    void givenCorruptedData_whenDecrypt_thenThrowsRuntimeException() {
        assertThatThrownBy(() -> converter.convertToEntityAttribute("NOT-VALID-BASE64-DATA!!!###"))
                .isInstanceOf(RuntimeException.class);
    }
}
