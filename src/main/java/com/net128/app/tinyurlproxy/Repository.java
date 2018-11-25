package com.net128.app.tinyurlproxy;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.repository.CrudRepository;

import java.util.Random;

public interface Repository extends CrudRepository<TinyUrl, Long> {
    Random random = new Random();
    int uniqueHashAttempts=10;

    TinyUrl findByHashedKey(String hashedKey);
    TinyUrl findByUrl(String url);

    default TinyUrl save(String url, int maxHHashKeyLength) {
        TinyUrl tinyUrl = new TinyUrl(url);
        String hashKey=null;
        for(int i=0;i<uniqueHashAttempts;i++) {
            hashKey=randomLowerCaseString(maxHHashKeyLength);
            if(findByHashedKey(hashKey)==null) {
                break;
            }
            hashKey=null;
        }
        if(hashKey==null) {
            throw new DuplicateKeyException("Could not find a new unique hash in "
                + uniqueHashAttempts+" attempts");
        }
        tinyUrl.setHashedKey(hashKey);
        tinyUrl = save(tinyUrl);
        return tinyUrl;
    }

    default String randomLowerCaseString(int length) {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = length;
        StringBuilder buffer = new StringBuilder(targetStringLength);
        for (int i = 0; i < targetStringLength; i++) {
            int randomLimitedInt = leftLimit + (int)
                    (random.nextFloat() * (rightLimit - leftLimit + 1));
            buffer.append((char) randomLimitedInt);
        }

        return buffer.toString();
    }
}
