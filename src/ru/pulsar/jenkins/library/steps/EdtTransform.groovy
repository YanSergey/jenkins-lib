package ru.pulsar.jenkins.library.steps

import ru.pulsar.jenkins.library.IStepExecutor
import ru.pulsar.jenkins.library.configuration.JobConfiguration
import ru.pulsar.jenkins.library.ioc.ContextRegistry
import ru.pulsar.jenkins.library.utils.Logger

class EdtTransform implements Serializable {

    public static final String PROJECT_NAME = 'temp'
    public static final String WORKSPACE = 'build/edt-workspace'
    public static final String WORKSPACE_ZIP = 'build/edt-workspace.zip'
    public static final String WORKSPACE_ZIP_STASH = 'edt-workspace-zip'

    private final JobConfiguration config;

    EdtTransform(JobConfiguration config) {
        this.config = config
    }

    def run() {
        IStepExecutor steps = ContextRegistry.getContext().getStepExecutor()

        Logger.printLocation()

        if (!config.stageFlags.edtValidate) {
            Logger.println("EDT validate step is disabled. No transform is needed.")
            return
        }

        def env = steps.env();

        def workspaceDir = "$env.WORKSPACE/$WORKSPACE"
        def configurationRoot = new File(env.WORKSPACE, config.srcDir).getAbsolutePath()

        steps.createDir(workspaceDir)

        Logger.println("Конвертация исходников из формата конфигуратора в формат EDT")

        def ringCommand = "ring edt workspace import --configuration-files '$configurationRoot' --project-name $PROJECT_NAME --workspace-location '$workspaceDir'"

        def ringOpts = ['RING_OPTS=-Dfile.encoding=UTF-8 -Dosgi.nl=ru -Duser.language=ru']
        steps.withEnv(ringOpts) {
            steps.cmd(ringCommand)
        }

        steps.zip(WORKSPACE, WORKSPACE_ZIP)
        steps.stash(WORKSPACE_ZIP_STASH, WORKSPACE_ZIP)
    }
}
