function plotMario(agent, alpha, vdim, gamma, epsilon, iv, training, episodes, steps, saveDir)

    %bijvoorbeeld:
    %QLearnAgent_Alpha0.3Gamma0.9Epsilon0.1Training100Runs10.txt
    fileName = strcat(agent, '_',...
        'A',num2str(alpha),...
        'VDim',num2str(vdim),...
        'G',num2str(gamma),...
        'E',num2str(epsilon),...
        'IV',num2str(iv),...
        'Training',num2str(training),...
        'Eps',num2str(episodes),...
        'Steps',num2str(steps) );
    %data = importdata(strcat(fileName, '.txt')); %TODO importdata for matlab
    data = load(strcat(fileName, '.txt'));
    
    averageDistance = data(1,:);
    stdDistance = data(2,:);
    averageReward = data(3,:);
    stdReward = data(4,:);
    %averageCompetitionScore = data(5,:);
    %stdCompetitionScore = data(6,:);
    
    h = figure();
    axis('tight');

    % plot our reward over the training episodes
    subplot(2,1,1)
    plotGP(0:steps:training, averageReward, stdReward);
    title(strcat(agent,...
        ',ViewDim = ',num2str(vdim),...
        ', alpha = ',num2str(alpha),...
        ', epsilon = ',num2str(epsilon),...
        ', initial value = ',num2str(iv),...
        ', gamma = ', num2str(gamma) ));
    xlabel('Training episodes');
    ylabel('Reward');
    
    
    % plot the distance over the training episodes
    subplot(2,1,2);
    plotGP(0:steps:training, averageDistance, stdDistance);
    xlabel('Training episodes');
    ylabel('Distance travelled');
    
    %{
    % plot the official competition score
    subplot(2,1,3);
    plotGP(1:length(averageCompetitionScore), averageCompetitionScore, stdCompetitionScore);
    xlabel('Training episodes');
    ylabel('Distance travelled');
    title('Competition score')
    %}
    
    % save the plot to pdf
    saveName = strcat(saveDir, fileName, '.pdf');
    saveas(h, saveName, 'pdf'); %TODO this is matlab, not Octave
    %print(saveName);
    
end
