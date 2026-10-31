package com.winwin.auth.process;

import com.winwin.auth.user.UserAccount;
import com.winwin.auth.user.UserAccountRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProcessingService {

    private final UserAccountRepository users;
    private final ProcessingLogRepository logs;
    private final DataApiClient dataApiClient;

    public ProcessingService(UserAccountRepository users,
                             ProcessingLogRepository logs,
                             DataApiClient dataApiClient) {
        this.users = users;
        this.logs = logs;
        this.dataApiClient = dataApiClient;
    }

    @Transactional
    public String process(String email, String input) {
        UserAccount user = users.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Authenticated user no longer exists"));
        String output = dataApiClient.transform(input);
        logs.save(new ProcessingLog(user, input, output));
        return output;
    }
}
