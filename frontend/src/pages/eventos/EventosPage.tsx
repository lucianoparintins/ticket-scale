import React, { useEffect, useState } from 'react';
import { api } from '../../services/api';
import { Plus, Trash2 } from 'lucide-react';

interface Evento {
  id: string;
  nome: string;
  descricao: string;
  dataInicio: string;
  dataFim: string;
  ativo: boolean;
}

const EventosPage: React.FC = () => {
  const [eventos, setEventos] = useState<Evento[]>([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [novoEvento, setNovoEvento] = useState({
    nome: '',
    descricao: '',
    dataInicio: '',
    dataFim: '',
  });

  const fetchEventos = async () => {
    try {
      const data = await api.get('/eventos');
      setEventos(data);
    } catch (err) {
      console.error('Erro ao buscar eventos', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchEventos();
  }, []);

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      // Format dates to ISO if needed, though input type datetime-local is usually fine
      await api.post('/eventos', novoEvento);
      setShowForm(false);
      setNovoEvento({ nome: '', descricao: '', dataInicio: '', dataFim: '' });
      fetchEventos();
    } catch (err) {
      alert('Erro ao criar evento');
    }
  };

  const handleDelete = async (id: string) => {
    if (window.confirm('Tem certeza que deseja desativar este evento?')) {
      try {
        await api.delete(`/eventos/${id}`);
        fetchEventos();
      } catch (err) {
        alert('Erro ao desativar evento');
      }
    }
  };

  if (loading) return <div>Carregando...</div>;

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <h1>Eventos</h1>
        <button className="btn btn-primary" style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }} onClick={() => setShowForm(!showForm)}>
          <Plus size={20} />
          Novo Evento
        </button>
      </div>

      {showForm && (
        <div className="card">
          <h3>Cadastrar Novo Evento</h3>
          <form onSubmit={handleCreate} style={{ marginTop: '1rem' }}>
            <div className="grid" style={{ gridTemplateColumns: '1fr 1fr' }}>
              <div className="form-group">
                <label>Nome</label>
                <input
                  type="text"
                  className="form-control"
                  value={novoEvento.nome}
                  onChange={(e) => setNovoEvento({ ...novoEvento, nome: e.target.value })}
                  required
                />
              </div>
              <div className="form-group">
                <label>Descrição</label>
                <input
                  type="text"
                  className="form-control"
                  value={novoEvento.descricao}
                  onChange={(e) => setNovoEvento({ ...novoEvento, descricao: e.target.value })}
                  required
                />
              </div>
              <div className="form-group">
                <label>Data Início</label>
                <input
                  type="datetime-local"
                  className="form-control"
                  value={novoEvento.dataInicio}
                  onChange={(e) => setNovoEvento({ ...novoEvento, dataInicio: e.target.value })}
                  required
                />
              </div>
              <div className="form-group">
                <label>Data Fim</label>
                <input
                  type="datetime-local"
                  className="form-control"
                  value={novoEvento.dataFim}
                  onChange={(e) => setNovoEvento({ ...novoEvento, dataFim: e.target.value })}
                  required
                />
              </div>
            </div>
            <div style={{ display: 'flex', gap: '1rem', marginTop: '1rem' }}>
              <button type="submit" className="btn btn-primary">Salvar Evento</button>
              <button type="button" className="btn" onClick={() => setShowForm(false)}>Cancelar</button>
            </div>
          </form>
        </div>
      )}

      <div className="card">
        <table className="table">
          <thead>
            <tr>
              <th>Nome</th>
              <th>Data Início</th>
              <th>Data Fim</th>
              <th>Status</th>
              <th>Ações</th>
            </tr>
          </thead>
          <tbody>
            {eventos.map((evento) => (
              <tr key={evento.id}>
                <td>{evento.nome}</td>
                <td>{new Date(evento.dataInicio).toLocaleString('pt-BR')}</td>
                <td>{new Date(evento.dataFim).toLocaleString('pt-BR')}</td>
                <td>
                  <span style={{ 
                    padding: '0.25rem 0.5rem', 
                    borderRadius: '9999px', 
                    fontSize: '0.75rem', 
                    backgroundColor: evento.ativo ? '#dcfce7' : '#fee2e2',
                    color: evento.ativo ? '#15803d' : '#991b1b'
                  }}>
                    {evento.ativo ? 'Ativo' : 'Inativo'}
                  </span>
                </td>
                <td>
                  <button 
                    className="btn btn-danger" 
                    style={{ padding: '0.25rem', display: 'flex', alignItems: 'center' }}
                    onClick={() => handleDelete(evento.id)}
                    disabled={!evento.ativo}
                  >
                    <Trash2 size={16} />
                  </button>
                </td>
              </tr>
            ))}
            {eventos.length === 0 && (
              <tr>
                <td colSpan={5} style={{ textAlign: 'center', color: 'var(--text-muted)' }}>Nenhum evento encontrado.</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default EventosPage;
