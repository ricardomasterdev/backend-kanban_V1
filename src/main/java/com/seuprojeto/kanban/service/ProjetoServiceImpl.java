package com.seuprojeto.kanban.service;

import com.seuprojeto.kanban.domain.Projeto;
import com.seuprojeto.kanban.domain.RegraNegocioException;
import com.seuprojeto.kanban.domain.StatusProjeto;
import com.seuprojeto.kanban.dto.ProjetoCreateUpdateDTO;
import com.seuprojeto.kanban.mapper.ProjetoMapper;
import com.seuprojeto.kanban.repository.ProjetoRepository;
import com.seuprojeto.kanban.repository.ResponsavelRepository;
import com.seuprojeto.kanban.repository.SecretariaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class ProjetoServiceImpl implements ProjetoService {

    private final ProjetoRepository repo;
    private final ResponsavelRepository responsavelRepo;
    private final SecretariaRepository secretariaRepo;
    private final ProjetoMapper mapper;

    public ProjetoServiceImpl(ProjetoRepository repo,
                              ResponsavelRepository responsavelRepo,
                              SecretariaRepository secretariaRepo,
                              ProjetoMapper mapper) {
        this.repo = repo;
        this.responsavelRepo = responsavelRepo;
        this.secretariaRepo = secretariaRepo;
        this.mapper = mapper;
    }

    @Override
    public Projeto criar(ProjetoCreateUpdateDTO dto) {
        Projeto p = mapper.toEntity(dto);
        aplicarVinculos(p, dto);
        LocalDate hoje = DateUtils.today();
        recalcularMetricas(p, hoje);
        p.setStatus(recalcularStatus(p, hoje));
        return repo.save(p);
    }

    @Override
    public Projeto atualizar(UUID id, ProjetoCreateUpdateDTO dto) {
        Projeto p = obter(id);
        p.setNome(dto.nome());
        p.setInicioPrevisto(dto.inicioPrevisto());
        p.setTerminoPrevisto(dto.terminoPrevisto());
        p.setInicioRealizado(dto.inicioRealizado());
        p.setTerminoRealizado(dto.terminoRealizado());
        aplicarVinculos(p, dto);
        LocalDate hoje = DateUtils.today();
        recalcularMetricas(p, hoje);
        p.setStatus(recalcularStatus(p, hoje));
        return p;
    }

    // ANTIGA (Kanban usa)
    @Override
    @Transactional(readOnly = true)
    public Page<Projeto> listar(String texto, StatusProjeto status, Pageable pageable) {
        String q = (texto == null || texto.isBlank()) ? null : texto.trim();
        if (q != null && status != null) {
            return repo.findByNomeContainingIgnoreCaseAndStatus(q, status, pageable);
        }
        if (q != null) {
            return repo.findByNomeContainingIgnoreCase(q, pageable);
        }
        if (status != null) {
            return repo.findByStatus(status, pageable);
        }
        return repo.findAll(pageable);
    }

    // NOVA (front usa q + secretariaId + status)
    @Override
    @Transactional(readOnly = true)
    public Page<Projeto> listar(String texto, StatusProjeto status, Long secretariaId, Pageable pageable) {
        String q = (texto == null || texto.isBlank()) ? null : texto.trim();

        if (q != null && secretariaId != null && status != null) {
            return repo.findByNomeContainingIgnoreCaseAndSecretaria_IdAndStatus(q, secretariaId, status, pageable);
        }
        if (q != null && secretariaId != null) {
            return repo.findByNomeContainingIgnoreCaseAndSecretaria_Id(q, secretariaId, pageable);
        }
        if (q != null && status != null) {
            return repo.findByNomeContainingIgnoreCaseAndStatus(q, status, pageable);
        }
        if (secretariaId != null && status != null) {
            return repo.findBySecretaria_IdAndStatus(secretariaId, status, pageable);
        }
        if (q != null) {
            return repo.findByNomeContainingIgnoreCase(q, pageable);
        }
        if (secretariaId != null) {
            return repo.findBySecretaria_Id(secretariaId, pageable);
        }
        if (status != null) {
            return repo.findByStatus(status, pageable);
        }
        return repo.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Projeto obter(UUID id) {
        return repo.findById(id).orElseThrow(() -> new NoSuchElementException("Projeto não encontrado"));
    }

    @Override
    public void excluir(UUID id) {
        repo.deleteById(id);
    }

    @Override
    public Projeto transicionar(UUID id, StatusProjeto para) {
        Projeto p = obter(id);
        LocalDate hoje = DateUtils.today();
        StatusProjeto de = p.getStatus();

        if (de == para) {
            recalcularMetricas(p, hoje);
            return p;
        }

        String msgEmAndamento_Atrasado = "Para ir a ATRASADO, ajuste datas para passado OU remova 'Início Realizado'.";
        String msgAtrasado_AIniciar    = "Remova 'Início Realizado' e ajuste Início/Término Previsto para datas futuras para voltar a A_INICIAR.";
        String msgAtrasado_EmAnd       = "Ajuste Início/Término Previsto para datas futuras para seguir a EM_ANDAMENTO.";
        String msgConcluido_AIniciar   = "Remova 'Término Realizado' e ajuste Início/Término Previsto para datas futuras para voltar a A_INICIAR.";

        switch (de) {
            case A_INICIAR -> {
                if (para == StatusProjeto.EM_ANDAMENTO) {
                    p.setInicioRealizado(hoje);
                } else if (para == StatusProjeto.ATRASADO) {
                    if (p.getInicioPrevisto() != null && hoje.isBefore(p.getInicioPrevisto())) {
                        throw new RegraNegocioException("Não é possível marcar ATRASADO antes do início previsto.");
                    }
                } else if (para == StatusProjeto.CONCLUIDO) {
                    p.setTerminoRealizado(hoje);
                }
            }
            case EM_ANDAMENTO -> {
                if (para == StatusProjeto.A_INICIAR) {
                    p.setInicioRealizado(null);
                } else if (para == StatusProjeto.ATRASADO) {
                    throw new RegraNegocioException(msgEmAndamento_Atrasado);
                } else if (para == StatusProjeto.CONCLUIDO) {
                    p.setTerminoRealizado(hoje);
                }
            }
            case ATRASADO -> {
                if (para == StatusProjeto.A_INICIAR) {
                    throw new RegraNegocioException(msgAtrasado_AIniciar);
                } else if (para == StatusProjeto.EM_ANDAMENTO) {
                    throw new RegraNegocioException(msgAtrasado_EmAnd);
                } else if (para == StatusProjeto.CONCLUIDO) {
                    p.setTerminoRealizado(hoje);
                }
            }
            case CONCLUIDO -> {
                if (para == StatusProjeto.A_INICIAR) {
                    throw new RegraNegocioException(msgConcluido_AIniciar);
                } else if (para == StatusProjeto.EM_ANDAMENTO) {
                    var bak = p.getTerminoRealizado();
                    p.setTerminoRealizado(null);
                    if (recalcularStatus(p, hoje) == StatusProjeto.ATRASADO) {
                        p.setTerminoRealizado(bak);
                        throw new RegraNegocioException("Não é possível reabrir para EM_ANDAMENTO: ao remover o término, o projeto fica ATRASADO. Ajuste as datas previstas.");
                    }
                } else if (para == StatusProjeto.ATRASADO) {
                    var bak = p.getTerminoRealizado();
                    p.setTerminoRealizado(null);
                    if (recalcularStatus(p, hoje) != StatusProjeto.ATRASADO) {
                        p.setTerminoRealizado(bak);
                        throw new RegraNegocioException("Não é possível mover para ATRASADO: ao remover o término, o projeto não fica ATRASADO. Ajuste as datas previstas.");
                    }
                }
            }
        }

        recalcularMetricas(p, hoje);
        StatusProjeto finalStatus = recalcularStatus(p, hoje);
        if (finalStatus != para) {
            throw new RegraNegocioException("Transição inválida: após ajustes, projeto ficou em " + finalStatus + ". Dica: revise datas previstas/realizadas conforme a regra.");
        }
        p.setStatus(finalStatus);
        return p;
    }

    private void aplicarVinculos(Projeto p, ProjetoCreateUpdateDTO dto) {
        if (dto.secretariaId() != null) {
            var s = secretariaRepo.findById(dto.secretariaId())
                    .orElseThrow(() -> new RegraNegocioException("Secretaria não encontrada"));
            p.setSecretaria(s);
        } else {
            p.setSecretaria(null);
        }

        p.getResponsaveis().clear();
        Set<Long> ids = dto.responsaveisIds();
        if (ids != null && !ids.isEmpty()) {
            var rs = responsavelRepo.findAllById(ids);
            p.getResponsaveis().addAll(new java.util.HashSet<>(rs));
        }
    }

    public StatusProjeto recalcularStatus(Projeto p, LocalDate hoje) {
        if (p.getTerminoRealizado() != null) return StatusProjeto.CONCLUIDO;
        boolean inicioReal = p.getInicioRealizado() != null;
        boolean terminoReal = p.getTerminoRealizado() != null;
        var inicioPrev = p.getInicioPrevisto();
        var terminoPrev = p.getTerminoPrevisto();

        if ((inicioPrev != null && inicioPrev.isBefore(hoje) && !inicioReal) ||
                (terminoPrev != null && terminoPrev.isBefore(hoje) && !terminoReal)) {
            return StatusProjeto.ATRASADO;
        }
        if (inicioReal && (terminoPrev == null || !terminoPrev.isBefore(hoje)) && !terminoReal) {
            return StatusProjeto.EM_ANDAMENTO;
        }
        return StatusProjeto.A_INICIAR;
    }

    public void recalcularMetricas(Projeto p, LocalDate hoje) {
        int percentual = 0;
        if (p.getInicioPrevisto() != null && p.getTerminoPrevisto() != null && p.getTerminoRealizado() == null) {
            int total = Math.max(0, DateUtils.daysBetween(p.getInicioPrevisto(), p.getTerminoPrevisto()));
            int usado = Math.max(0, DateUtils.daysBetween(p.getInicioPrevisto(), hoje));
            int restante = Math.max(0, total - usado);
            if (total > 0 && !hoje.isAfter(p.getTerminoPrevisto())) {
                percentual = Math.round(restante * 100f / total);
            }
        }
        p.setPercentualTempoRestante(percentual);

        int atraso = 0;
        if (p.getTerminoRealizado() == null &&
                p.getTerminoPrevisto() != null &&
                p.getTerminoPrevisto().isBefore(hoje)) {
            atraso = Math.max(0, DateUtils.daysBetween(p.getTerminoPrevisto(), hoje));
        }
        p.setDiasAtraso(atraso);
    }
}
