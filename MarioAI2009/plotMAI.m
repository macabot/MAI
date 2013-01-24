function plotMAI()0
    close all;

    % initialising values used by q-learning / sarsa
    agent = 'QLearnAgent';
    alpha = 0.3;
    gamma = 0.9;
    epsilon = 0.1;
    %state = '';
    training = 1000; % amount of times that training is done
    runs = 10; % averaging the result over 10 runs
    saveDir = '../MAIPDFs/';
    
    plotMario(agent, alpha, gamma, epsilon, training, runs, saveDir);

end 
