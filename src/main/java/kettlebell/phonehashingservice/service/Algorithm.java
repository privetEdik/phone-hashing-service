package kettlebell.phonehashingservice.service;

import lombok.Getter;

@Getter
public enum Algorithm {
    SHA1("SHA-1"),
    SHA2("SHA-256"),
    SHA3("SHA3-256");

    private final String algorithm;

    Algorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public static Algorithm fromName(String name) {
        for (Algorithm alg : values()) {
            if (alg.name().equalsIgnoreCase(name)) {
                return alg;
            }
        }
        throw new IllegalArgumentException("Invalid algorithm name: " + name);
    }
}
