-- =====================================================================
-- 1) Função: calcula dias_atraso (e atualiza updated_at, opcional)
-- =====================================================================
CREATE OR REPLACE FUNCTION public.fn_projeto_calc_dias_atraso()
RETURNS trigger
LANGUAGE plpgsql
AS $$
DECLARE
v_dias1 integer := 0;
  v_dias2 integer := 0;
BEGIN
  -- (opcional) manter updated_at sempre coerente no DML
  NEW.updated_at := NOW();

  -- Caso 1: não iniciou e início previsto já passou
  IF NEW.inicio_previsto IS NOT NULL
     AND NEW.inicio_realizado IS NULL
     AND NEW.inicio_previsto < CURRENT_DATE THEN
    v_dias1 := GREATEST( (CURRENT_DATE - NEW.inicio_previsto), 0 );
END IF;

  -- Caso 2: não concluiu e término previsto já passou
  IF NEW.termino_previsto IS NOT NULL
     AND NEW.termino_realizado IS NULL
     AND NEW.termino_previsto < CURRENT_DATE THEN
    v_dias2 := GREATEST( (CURRENT_DATE - NEW.termino_previsto), 0 );
END IF;

  NEW.dias_atraso := GREATEST(COALESCE(v_dias1,0), COALESCE(v_dias2,0));

RETURN NEW;
END;
$$;

-- =====================================================================
-- 2) Trigger: executa antes de INSERT/UPDATE na tabela projeto
-- =====================================================================
DO $$
BEGIN
  -- remove trigger anterior se existir (idempotente)
  IF EXISTS (
    SELECT 1 FROM pg_trigger
    WHERE tgname = 'trg_projeto_calc_dias_atraso'
  ) THEN
DROP TRIGGER trg_projeto_calc_dias_atraso ON public.projeto;
END IF;
END;
$$;

CREATE TRIGGER trg_projeto_calc_dias_atraso
    BEFORE INSERT OR UPDATE OF
    inicio_previsto, inicio_realizado,
                         termino_previsto, termino_realizado,
                         status -- (opcional: se o status mudar, reavalia também)
                     ON public.projeto
                         FOR EACH ROW
                         EXECUTE FUNCTION public.fn_projeto_calc_dias_atraso();

-- =====================================================================
-- 3) Backfill: recalcula dias_atraso para os registros existentes
--    (rode uma vez após criar a função/trigger)
-- =====================================================================
UPDATE public.projeto p
SET dias_atraso = GREATEST(
        COALESCE(
                CASE
                    WHEN p.inicio_previsto < CURRENT_DATE AND p.inicio_realizado IS NULL
                        THEN GREATEST((CURRENT_DATE - p.inicio_previsto), 0)
                    ELSE 0
                    END, 0
        ),
        COALESCE(
                CASE
                    WHEN p.termino_previsto < CURRENT_DATE AND p.termino_realizado IS NULL
                        THEN GREATEST((CURRENT_DATE - p.termino_previsto), 0)
                    ELSE 0
                    END, 0
        )
                  ),
    updated_at = NOW();
